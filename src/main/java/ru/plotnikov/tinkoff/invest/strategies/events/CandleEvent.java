package ru.plotnikov.tinkoff.invest.strategies.events;

public interface CandleEvent extends ExchangeEvent {

    long getPricePennies();

    String getFigi();

}
