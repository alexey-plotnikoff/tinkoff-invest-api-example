package ru.plotnikov.tinkoff.invest.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import ru.plotnikov.tinkoff.invest.controller.dto.StatDto;
import ru.plotnikov.tinkoff.invest.stat.Stat;
import ru.plotnikov.tinkoff.invest.strategies.TradingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller("/stat")
public class StatController {

    private final TradingStrategy tradingStrategy;

    public StatController(TradingStrategy tradingStrategy) {
        this.tradingStrategy = tradingStrategy;
    }

    @Get(produces = MediaType.APPLICATION_JSON)
    public List<StatDto> index() {
        Map<String, Stat> statistic = tradingStrategy.getStatistic();
        List<StatDto> result = new ArrayList<>();
        for (Map.Entry<String, Stat> entry : statistic.entrySet()) {
            result.add(
                    new StatDto(entry.getKey(), entry.getValue().getOperations(),entry.getValue().getTotalProfit())
            );
        }

        return result;
    }
}
