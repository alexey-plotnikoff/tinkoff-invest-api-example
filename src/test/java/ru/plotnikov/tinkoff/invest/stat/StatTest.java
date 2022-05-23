package ru.plotnikov.tinkoff.invest.stat;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import ru.plotnikov.tinkoff.invest.strategies.Action;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class StatTest {

    @Test
    void totalProfitNoOperation() {
        // SETUP
        Stat stat = new Stat();

        // WHEN
        long total = stat.getTotalProfit();

        // THEN
        assertEquals(0, total);
    }

    @Test
    void totalProfitOneOperation() {
        // SETUP
        Stat stat = new Stat();
        stat.getOperations().add(new StatOperation(Action.BUY, 2182000));

        // WHEN
        long total = stat.getTotalProfit();

        // THEN
        assertEquals(0, total);
    }

    @Test
    void totalProfitTwoOperation() {
        // SETUP
        Stat stat = new Stat();
        stat.getOperations().add(new StatOperation(Action.BUY, 2182000));
        stat.getOperations().add(new StatOperation(Action.SELL, 2227400));

        // WHEN
        long total = stat.getTotalProfit();

        // THEN
        assertEquals(45400, total);
    }

    @Test
    void totalProfitThreeOperation() {
        // SETUP
        Stat stat = new Stat();
        stat.getOperations().add(new StatOperation(Action.BUY, 2182000));
        stat.getOperations().add(new StatOperation(Action.SELL, 2227400));
        stat.getOperations().add(new StatOperation(Action.BUY, 2181200));

        // WHEN
        long total = stat.getTotalProfit();

        // THEN
        assertEquals(45400, total);
    }

    @Test
    void totalProfitFourOperation() {
        // SETUP
        Stat stat = new Stat();
        stat.getOperations().add(new StatOperation(Action.BUY, 2182000));
        stat.getOperations().add(new StatOperation(Action.SELL, 2227400));
        stat.getOperations().add(new StatOperation(Action.BUY, 2181200));
        stat.getOperations().add(new StatOperation(Action.SELL, 2225800));

        // WHEN
        long total = stat.getTotalProfit();

        // THEN
        assertEquals(90000, total);
    }

}
