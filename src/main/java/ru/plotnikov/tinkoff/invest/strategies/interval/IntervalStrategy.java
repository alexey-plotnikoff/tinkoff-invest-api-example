package ru.plotnikov.tinkoff.invest.strategies.interval;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.plotnikov.tinkoff.invest.service.InvestApiService;
import ru.plotnikov.tinkoff.invest.stat.Stat;
import ru.plotnikov.tinkoff.invest.stat.StatOperation;
import ru.plotnikov.tinkoff.invest.strategies.events.CandleEvent;
import ru.plotnikov.tinkoff.invest.strategies.events.ExchangeEvent;
import ru.plotnikov.tinkoff.invest.strategies.Action;
import ru.plotnikov.tinkoff.invest.strategies.TradingStrategy;
import ru.plotnikov.tinkoff.invest.utils.MoneyUtils;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PortfolioPosition;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Requires(property = "trading.algorithm.type", value = IntervalStrategy.CODE)
@Requires(notEnv = "test")
public class IntervalStrategy implements TradingStrategy {

    private final Logger log = LoggerFactory.getLogger(IntervalStrategy.class);

    public static final String CODE = "interval";

    private final InvestApiService investApiService;
    private final List<String> figiList;
    private final int corridorLength;
    private final int corridorRequiredPercentage;
    private final Date initDay;
    private final String accountId;
    private final Map<String, Integer> quantity;
    private volatile boolean initialized = false;
    private final Object monitor = new Object();

    private final Map<String, Corridor> corridorMap = new ConcurrentHashMap<>();
    private final Map<String, Stat> statistic = new ConcurrentHashMap<>();

    public IntervalStrategy(
            InvestApiService investApiService,
            @Value("${trading.figi}") List<String> figiList,
            @Value("${trading.algorithm.corridor-length}") int corridorLength,
            @Value("${trading.algorithm.corridor-percentage}") int corridorRequiredPercentage,
            @Value("${trading.algorithm.init-day}") Optional<Date> initDay,
            @Value("${trading.tinkoff.account-id}") String accountId,
            @Property(name = "trading.quantity") @MapFormat(keyFormat = StringConvention.RAW)
            Map<String, Integer> quantity
    ) {
        this.investApiService = investApiService;
        this.figiList = figiList;
        this.corridorLength = corridorLength;
        this.corridorRequiredPercentage = corridorRequiredPercentage;
        this.initDay = initDay.isEmpty() ? null : initDay.get();
        this.accountId = accountId;
        this.quantity = quantity;
    }

    @Scheduled(initialDelay = "1d", fixedDelay = "1d")
    public void reInit() {
        initialize(null);
    }

    @Scheduled(initialDelay = "1m", fixedDelay = "1m")
    public void generateEvent() {
        List<LastPrice> lastPricesList = investApiService.getApi().getMarketDataService().getLastPricesSync(figiList);
        for (LastPrice price : lastPricesList) {
            fire(new CandleEvent() {
                @Override
                public long getPricePennies() {
                    return MoneyUtils.toPennies(price.getPrice().getUnits(), price.getPrice().getNano());
                }

                @Override
                public String getFigi() {
                    return price.getFigi();
                }
            });
        }
    }

//    CANDLE_INTERVAL_1_MIN	  от 1 минут до 1 дня
//    CANDLE_INTERVAL_5_MIN	  от 5 минут до 1 дня
//    CANDLE_INTERVAL_15_MIN  от 15 минут до 1 дня
//    CANDLE_INTERVAL_HOUR    от 1 часа до 1 недели
//    CANDLE_INTERVAL_DAY     от 1 дня до 1 года
    @Async
    @EventListener
    public void initialize(StartupEvent event) {
        initialized = false;
        synchronized (monitor) {
            log.info("Strategy initialization started: [name = '{}']", "IntervalStrategy");
            corridorMap.clear();

            for (String figi : figiList) {
                ZonedDateTime now;
                if (initDay == null) {
                    now = ZonedDateTime.now();
                } else {
                    now = ZonedDateTime.ofInstant(initDay.toInstant(), ZoneId.systemDefault());
                }
                ZonedDateTime monthAgo = now.minus(corridorLength, ChronoUnit.DAYS);

                List<HistoricCandle> lastMonthCandleList = new ArrayList<>();

                while (monthAgo.isBefore(now)) {
                    log.info("Get candles: [figi = '{}', day = '{}']", figi, now);
                    lastMonthCandleList.addAll(
                            investApiService.getApi().getMarketDataService().getCandlesSync(
                                    figi,
                                    now.minus(1, ChronoUnit.DAYS).toInstant(),
                                    now.toInstant(),
                                    CandleInterval.CANDLE_INTERVAL_1_MIN
                            )
                    );

                    now = now.minus(1, ChronoUnit.DAYS);
                }

                long max = Long.MIN_VALUE;
                long min = Long.MAX_VALUE;
                for (HistoricCandle historicCandle : lastMonthCandleList) {
                    long current = MoneyUtils.toPennies(
                            historicCandle.getClose().getUnits(),
                            historicCandle.getClose().getNano()
                    );
                    max = Math.max(max, current);
                    min = Math.min(min, current);
                }

                int percentage = 100;
                long corridorMax = max;
                long corridorMin = min;
                while (percentage >= corridorRequiredPercentage) {
                    corridorMax = corridorMax - 1;
                    corridorMin = corridorMin + 1;
                    int valuesInCorridor = 0;
                    for (HistoricCandle historicCandle : lastMonthCandleList) {
                        long current = MoneyUtils.toPennies(
                                historicCandle.getClose().getUnits(),
                                historicCandle.getClose().getNano()
                        );
                        if (current >= corridorMin && current <= corridorMax) {
                            valuesInCorridor++;
                        }
                    }
                    percentage = (int) (((double) valuesInCorridor / lastMonthCandleList.size()) * 100);
                }

                Action nextAction = detectNextAction(figi);
                corridorMap.put(figi, new Corridor(corridorMin, corridorMax, nextAction));

                Stat stat = statistic.get(figi);
                if (stat == null) {
                    statistic.put(figi, new Stat());
                }

                log.info("Strategy initialization finished: [name = '{}', figi = '{}', next_action = '{}', min = {}, " +
                                "max = '{}', total_candles = '{}', corridor_min = '{}', corridor_max = '{}', " +
                                "percentage = '{}']",
                        "IntervalStrategy", figi, nextAction, min, max, lastMonthCandleList.size(), corridorMin,
                        corridorMax, percentage);
                initialized = true;
            }
        }
    }

    private Action detectNextAction(String figi) {
        if (investApiService.getApi().isSandboxMode()) {
            for (PortfolioPosition position : investApiService.getApi().getSandboxService()
                    .getPortfolioSync(accountId).getPositionsList()) {
                if (figi.equals(position.getFigi())) {
                    if (position.getQuantityLots().getUnits() == 0L && position.getQuantityLots().getNano() == 0) {
                        return Action.BUY;
                    } else {
                        return Action.SELL;
                    }
                }
                return Action.BUY;
            }
        } else {
            for (Position position : investApiService.getApi().getOperationsService()
                    .getPortfolioSync(accountId).getPositions()) {
                if (figi.equals(position.getFigi())) {
                    if (BigDecimal.ZERO.equals(position.getQuantityLots())) {
                        return Action.BUY;
                    } else {
                        return Action.SELL;
                    }
                }
                return Action.BUY;
            }
        }

        return null;
    }

    @Override
    public String getCode() {
        return IntervalStrategy.CODE;
    }

    @Override
    public void fire(ExchangeEvent event) {
        if (!(event instanceof CandleEvent)) {
            return;
        }
        CandleEvent candleEvent = (CandleEvent) event;
        if (!initialized) {
            synchronized (monitor) {
                // wait initialize
            }
        }

        Corridor corridor = corridorMap.get(candleEvent.getFigi());
        if (corridor == null) {
            log.warn("Corridor not initialized: [figi = '{}']", candleEvent.getFigi());
            return;
        }
        if (corridor.getNextAction() == null) {
            log.warn("Unknown next action: [figi = '{}']", candleEvent.getFigi());
            return;
        }

        OrderDirection direction = null;
        if (corridor.getNextAction() == Action.BUY) {
            if (candleEvent.getPricePennies() <= corridor.getCorridorMin()) {
                log.info("BUY: [figi = '{}', price = '{}']", candleEvent.getFigi(), candleEvent.getPricePennies());
                corridor.setNextAction(Action.SELL);
                Stat stat = statistic.get(candleEvent.getFigi());
                if (stat != null) {
                    // TODO не точная сумма, не учитывает PARTIAL
                    stat.getOperations().add(new StatOperation(Action.BUY, candleEvent.getPricePennies()));
                }
                direction = OrderDirection.ORDER_DIRECTION_BUY;
            }
        } else if (corridor.getNextAction() == Action.SELL) {
            if (candleEvent.getPricePennies() >= corridor.getCorridorMax()) {
                log.info("SELL: [figi = '{}', price = '{}']", candleEvent.getFigi(), candleEvent.getPricePennies());
                corridor.setNextAction(Action.BUY);
                Stat stat = statistic.get(candleEvent.getFigi());
                if (stat != null) {
                    // TODO не точная сумма, не учитывает PARTIAL
                    stat.getOperations().add(new StatOperation(Action.SELL, candleEvent.getPricePennies()));
                }
                direction = OrderDirection.ORDER_DIRECTION_SELL;
            }
        }

        if (direction != null) {
            Quotation quotation = Quotation.newBuilder()
                    .setUnits(MoneyUtils.toUnits(candleEvent.getPricePennies()))
                    .setNano(MoneyUtils.toNanos(candleEvent.getPricePennies()))
                    .build();
            if (investApiService.getApi().isSandboxMode()) {
                investApiService.getApi().getSandboxService().postOrderSync(
                        candleEvent.getFigi(),
                        quantity.get(candleEvent.getFigi()),
                        quotation,
                        direction,
                        accountId,
                        OrderType.ORDER_TYPE_MARKET,
                        // TODO add support for UUID
                        UUID.randomUUID().toString()
                );
            } else {
                investApiService.getApi().getOrdersService().postOrderSync(
                        candleEvent.getFigi(),
                        quantity.get(candleEvent.getFigi()),
                        quotation,
                        direction,
                        accountId,
                        OrderType.ORDER_TYPE_MARKET,
                        // TODO add support for UUID
                        UUID.randomUUID().toString()
                );
            }
        }
    }

    @Override
    public Map<String, Stat> getStatistic() {
        return statistic;
    }
}
