package ru.plotnikov.tinkoff.invest.utils;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class MoneyUtilsTest {

    @Test
    void convertToPennies() {
        // SETUP

        // WHEN
        long pennies = MoneyUtils.toPennies(5102L, 640000);

        // THEN
        assertEquals(510264, pennies);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 0",
            "10, 0",
            "99, 0",
            "101, 1",
            "150, 1",
            "199, 1",
            "56543, 565"
    })
    void convertToUnits(long sum, long result) {
        // WHEN
        long units = MoneyUtils.toUnits(sum);

        // THEN
        assertEquals(result, units);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 1",
            "10, 10",
            "99, 99",
            "101, 1",
            "150, 50",
            "199, 99",
            "56543, 43"
    })
    void convertToNanos(int sum, int result) {
        // WHEN
        int nanos = MoneyUtils.toNanos(sum);

        // THEN
        assertEquals(result, nanos);
    }

}