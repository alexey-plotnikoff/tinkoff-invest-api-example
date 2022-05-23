package ru.plotnikov.tinkoff.invest.controller.dto;

import io.micronaut.core.annotation.Introspected;
import ru.plotnikov.tinkoff.invest.stat.StatOperation;

import java.util.List;

@Introspected
public class StatDto {
    private String figi;
    private List<StatOperation> operations;
    private long profit;

    public StatDto(String figi, List<StatOperation> operations, long profit) {
        this.figi = figi;
        this.operations = operations;
        this.profit = profit;
    }

    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public List<StatOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<StatOperation> operations) {
        this.operations = operations;
    }

    public long getProfit() {
        return profit;
    }

    public void setProfit(long profit) {
        this.profit = profit;
    }
}
