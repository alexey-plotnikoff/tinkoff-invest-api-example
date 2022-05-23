package ru.plotnikov.tinkoff.invest.strategies;

import ru.plotnikov.tinkoff.invest.stat.Stat;
import ru.plotnikov.tinkoff.invest.strategies.events.ExchangeEvent;

import java.util.Map;

public interface TradingStrategy {

    String getCode();

    void fire(ExchangeEvent event);

    Map<String, Stat> getStatistic();
}
