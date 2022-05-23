package ru.plotnikov.tinkoff.invest.utils;

public final class MoneyUtils {

    private MoneyUtils() {

    }

    public static long toPennies(long units, int nano) {
        long value = units * 100;
        String penniesString = String.valueOf(nano);
        int pennies = Integer.parseInt(penniesString.substring(0, Math.min(2, penniesString.length())));
        return value + pennies;
    }

    public static long toUnits(long pennies) {
        return (long) (pennies / 100.D);
    }

    public static int toNanos(long pennies) {
        return (int) (pennies - ((long) (pennies / 100.D)) * 100);
    }
}
