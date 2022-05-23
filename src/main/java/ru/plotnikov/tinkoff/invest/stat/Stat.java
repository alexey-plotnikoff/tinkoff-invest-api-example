package ru.plotnikov.tinkoff.invest.stat;

import java.util.ArrayList;
import java.util.List;

public class Stat {

    private final List<StatOperation> operations;

    public Stat() {
        operations = new ArrayList<>();
    }

    public List<StatOperation> getOperations() {
        return operations;
    }

    public long getTotalProfit() {
        long total = 0;
        for (int i = 0; i < operations.size(); i = i + 2) {
            if (i + 1 < operations.size()) {
                total = total + -1 * (operations.get(i).getSum() - operations.get(i+1).getSum());
            }
        }
        return total;
    }
}
