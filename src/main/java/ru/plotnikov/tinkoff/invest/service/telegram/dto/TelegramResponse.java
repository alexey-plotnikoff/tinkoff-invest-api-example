package ru.plotnikov.tinkoff.invest.service.telegram.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class TelegramResponse<T> {
    private boolean ok;
    private T result;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
