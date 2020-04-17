package me.deecaad.core.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This utility class offers methods useful
 * for manipulating numbers as well as translating
 * larger numbers into more readable forms
 */
public class NumberUtils {

    // Generally used for enchantments in lore
    private static final TreeMap<Integer, String> numerals;

    // Used to display the amount of time passed
    private static final TreeMap<Integer, String> time;

    static {
        numerals = new TreeMap<>();
        numerals.put(1000, "M");
        numerals.put(900, "CM");
        numerals.put(500, "D");
        numerals.put(400, "CD");
        numerals.put(100, "C");
        numerals.put(90, "XC");
        numerals.put(50, "L");
        numerals.put(40, "XL");
        numerals.put(10, "X");
        numerals.put(9, "IX");
        numerals.put(5, "V");
        numerals.put(4, "IV");
        numerals.put(1, "I");

        // Each int is the number of seconds in the unit
        time = new TreeMap<>();
        time.put(31536000, "y");
        time.put(86400, "d");
        time.put(3600, "h");
        time.put(60, "m");
        time.put(1, "s");
    }

    /**
     * Don't let anyone instantiate this class
     */
    private NumberUtils() {
    }

    /**
     * Threadsafe method to generate
     * a random integer [0, length).
     * Useful for getting random elements
     * from collections and arrays.
     *
     * @param length The upper bound
     * @return The random number
     */
    public static int random(int length) {
        return ThreadLocalRandom.current().nextInt(length);
    }
    
    /**
     * Threadsafe method to generate
     * a random integer [min, max]
     *
     * @param min minimum size of the number
     * @param max maximum size of the number
     * @return random int between min and max
     */
    public static int random(int min, int max) {
        if (min == max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    
    /**
     * Threadsafe method to generate
     * a random double [min, max)
     *
     * @param min minimum size of the number
     * @param max maximum size of the number
     * @return random double between min and max
     */
    public static double  random(double min, double max) {
        if (min == max) return min;
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Checks if a chance (Which should be a
     * number [0, 1]) was "successful". The
     * behavior of this method will change
     * because of random number comparison
     *
     * @param chance The percentage chance to be successful
     * @return If the chance was successful or not
     */
    public static boolean chance(double chance) {
        return Math.random() < chance;
    }

    /**
     * Shorthand for calling both <code>Math.min</code> and
     * <code>Math.max</code>.
     *
     * @param min The minimum number the value can be
     * @param value The actual value to compare
     * @param max The maximum number the value can be
     * @return Whichever bound [min, max]
     */
    public static int minMax(int min, int value, int max) {
        if (min > value) {
            return min;
        } else if (max < value) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Shorthand for calling both <code>Math.min</code> and
     * <code>Math.max</code>.
     *
     * @param min The minimum number the value can be
     * @param value The actual value to compare
     * @param max The maximum number the value can be
     * @return Whichever bound [min, max]
     */
    public static double minMax(float min, float value, float max) {
        if (min > value) {
            return min;
        } else if (max < value) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Shorthand for calling both <code>Math.min</code> and
     * <code>Math.max</code>.
     *
     * @param min The minimum number the value can be
     * @param value The actual value to compare
     * @param max The maximum number the value can be
     * @return Whichever bound [min, max]
     */
    public static double minMax(double min, double value, double max) {
        if (min > value) {
            return min;
        } else if (max < value) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Determines if two doubles are close enough
     * in value to be considered equal. This is
     * important in math with doubles because of
     * inaccuracies with doubles
     *
     * @param a First double
     * @param b Second double
     * @return If they are equal
     */
    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < 1e-10;
    }

    /**
     * Recursive function that translates an
     * <code>int</code> number to a <code>String
     * </code> roman numeral.
     *
     * @param from Integer to translate
     * @return Roman numeral translation
     */
    public static String toRomanNumeral(int from) {
        int numeral = numerals.floorKey(from);
        if (from == numeral) {
            return numerals.get(from);
        }
        return numerals.get(numeral) + toRomanNumeral(from - numeral);
    }

    /**
     * Recursive function that translates an
     * <code>int</code> amount of seconds into
     * the smallest possible combination of
     * years, days, hours, minutes, and seconds
     *
     * @param seconds The number of seconds
     * @return Simplified number
     */
    public static String toTime(int seconds) {
        int unit = time.floorKey(seconds);
        int amount = seconds / unit;
        if (seconds % unit == 0) {
            return amount + time.get(unit);
        }
        return amount + time.get(unit) + " " + toTime(seconds - amount * unit);
    }

    /**
     * @param lastMillis the last millis something happened
     * @param amount the amount of millis required to pass since last millis
     * @return true only if enough millis have passed since last millis
     */
    public static boolean hasMillisPassed(long lastMillis, long amount) {
        return (System.currentTimeMillis() - lastMillis > amount);
    }

    /**
     * Rounds the value to given amount of significands.
     * Will also strip trailing zeros.
     *
     * @param value the version value to be rounded
     * @param significands the amount of significands in return value
     * @return value when rounded to decimals
     */
    public static double getAsRounded(double value, int significands) {
        if (value % 1 == 0) {
            return (int) value;
        }
        int intValue = (int) value;
        BigDecimal bigDecimal = new BigDecimal(value - intValue, new MathContext(significands, RoundingMode.HALF_UP));
        bigDecimal = bigDecimal.add(new BigDecimal(intValue));
        bigDecimal = bigDecimal.stripTrailingZeros();
        return Double.parseDouble(bigDecimal.toPlainString());
    }
}
