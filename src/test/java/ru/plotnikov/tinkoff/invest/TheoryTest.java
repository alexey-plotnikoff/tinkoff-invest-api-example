package ru.plotnikov.tinkoff.invest;

import io.micronaut.context.DefaultBeanContext;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.plotnikov.tinkoff.invest.service.InvestApiService;
import ru.plotnikov.tinkoff.invest.stat.Stat;
import ru.plotnikov.tinkoff.invest.strategies.events.CandleEvent;
import ru.plotnikov.tinkoff.invest.strategies.interval.IntervalStrategy;
import ru.plotnikov.tinkoff.invest.utils.MoneyUtils;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.core.SandboxService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Disabled
@MicronautTest
public class TheoryTest {

    private static final Logger log = LoggerFactory.getLogger(TheoryTest.class);

    @Inject InvestApiService investApiService;

    @Test
    void check() throws InterruptedException {
        // SETUP
        SandboxService sandboxService = investApiService.getApi().getSandboxService();
        String account = sandboxService.openAccountSync();
        sandboxService.payInSync(account, MoneyValue.newBuilder().setCurrency("RUB").setUnits(100000).build());
        List<String> figiList = List.of(
                "BBG004731489", //Норникель
                "BBG0013HGFT4"  //USD
//                "BBG004S683W7",  //Аэрофлот
//                "BBG0047315Y7"  //Сбер
        );
        Map<String, Integer> quantity = new HashMap<>();
        quantity.put("BBG004731489", 1);
        quantity.put("BBG0013HGFT4", 1);
//        quantity.put("BBG004S683W7", 1);
//        quantity.put("BBG0047315Y7", 1);
        ZonedDateTime startDay = ZonedDateTime.of(
                2022, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()
        );
        IntervalStrategy intervalStrategy = new IntervalStrategy(
                investApiService,
                figiList,
                7,
                80,
                Optional.of(Date.from(startDay.toInstant())),
                account,
                quantity
        );
        intervalStrategy.initialize(new StartupEvent(new DefaultBeanContext()));

        // WHEN
        for (String figi : figiList) {
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime day = startDay;
            while (day.isBefore(now)) {
                day = day.plusDays(1);
                List<HistoricCandle> candlesList = investApiService.getApi().getMarketDataService().getCandlesSync(
                        figi,
                        day.minus(1, ChronoUnit.DAYS).toInstant(),
                        day.toInstant(),
                        CandleInterval.CANDLE_INTERVAL_1_MIN
                );
                for (HistoricCandle candle : candlesList) {
                    intervalStrategy.fire(new CandleEvent() {
                        @Override
                        public long getPricePennies() {
                            return MoneyUtils.toPennies(
                                    candle.getClose().getUnits(), candle.getClose().getNano()
                            );
                        }

                        @Override
                        public String getFigi() {
                            return figi;
                        }
                    });
                }
            }
        }

        // THEN
        Map<String, Stat> statistic = intervalStrategy.getStatistic();
        for (Map.Entry<String, Stat> entry : statistic.entrySet()) {
            log.info("Profit: [figi = '{}', total = '{}', operations = '{}']",
                    entry.getKey(), entry.getValue().getTotalProfit(), entry.getValue().getOperations());
        }

        // CLEANUP
        sandboxService.closeAccount(account);
    }
}
