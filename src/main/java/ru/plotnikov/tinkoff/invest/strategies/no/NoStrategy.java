package ru.plotnikov.tinkoff.invest.strategies.no;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import ru.plotnikov.tinkoff.invest.stat.Stat;
import ru.plotnikov.tinkoff.invest.strategies.TradingStrategy;
import ru.plotnikov.tinkoff.invest.strategies.events.ExchangeEvent;

import java.util.Map;

@Singleton
@Requires(property = "trading.algorithm.type", value = NoStrategy.CODE)
public class NoStrategy implements TradingStrategy {

    public static final String CODE = "no";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void fire(ExchangeEvent event) {

    }

    @Override
    public Map<String, Stat> getStatistic() {
        return null;
    }
}
