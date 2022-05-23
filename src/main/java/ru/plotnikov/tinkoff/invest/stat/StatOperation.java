package ru.plotnikov.tinkoff.invest.stat;

import ru.plotnikov.tinkoff.invest.strategies.Action;

public class StatOperation {
    private Action action;
    private long sum;

    public StatOperation(Action action, long sum) {
        this.action = action;
        this.sum = sum;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "StatOperation{" +
                "action=" + action +
                ", sum=" + sum +
                '}';
    }
}
