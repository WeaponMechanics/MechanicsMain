package me.deecaad.core.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This final utility class consists of static methods that operate on or
 * return numbers. This class also contains methods for randomization, and for
 * translating <i>complicated</i> numbers into more readable forms.
 *
 * The methods of this class are threadsafe.
 */
public final class NumberUtil {

    public static final long HOUR_IN_TICKS = 72000;
    private static final TreeMap<Integer, String> NUMERALS;
    private static final TreeMap<Integer, String> TIME;

    static {
        NUMERALS = new TreeMap<>();
        NUMERALS.put(1000, "M");
        NUMERALS.put(900, "CM");
        NUMERALS.put(500, "D");
        NUMERALS.put(400, "CD");
        NUMERALS.put(100, "C");
        NUMERALS.put(90, "XC");
        NUMERALS.put(50, "L");
        NUMERALS.put(40, "XL");
        NUMERALS.put(10, "X");
        NUMERALS.put(9, "IX");
        NUMERALS.put(5, "V");
        NUMERALS.put(4, "IV");
        NUMERALS.put(1, "I");

        // Each integer is the number of seconds in the unit.
        TIME = new TreeMap<>();
        TIME.put(31536000, "y");
        TIME.put(86400, "d");
        TIME.put(3600, "h");
        TIME.put(60, "m");
        TIME.put(1, "s");
    }

    // Don't let anyone instantiate this class.
    private NumberUtil() {
    }

    /**
     * Threadsafe method to get a number <code>[0, length)</code>. Useful for
     * getting a random index of an ordered data structure.
     *
     * @param length The upper bound, exclusive.
     * @return The random index.
     * @throws IllegalArgumentException If <code>length <= 0</code>.
     */
    public static int random(int length) {
        return ThreadLocalRandom.current().nextInt(length);
    }

    /**
     * Returns a random element from the array of the array's type. This method
     * is threadsafe.
     *
     * @param arr The non-null array to grab an element from.
     * @param <T> The generic type of the array.
     * @return The random element. If the array has no <code>null</code>
     * elements, then this method will not return <code>null</code>.
     * @throws IllegalArgumentException If <code>arr.length == 0</code>.
     */
    public static <T> T random(T[] arr) {
        return arr[random(arr.length)];
    }

    /**
     * Returns a random element from the list. The returned element will be of
     * the list's generic type. This method is threadsafe.
     *
     * @param list The non-null list of elements.
     * @param <T>  The generic type of the list.
     * @return The random element. If the list has no <code>null</code>
     * elements, then this method will not return <code>null</code>.
     */
    public static <T> T random(List<T> list) {
        return list.get(random(list.size()));
    }

    /**
     * Returns a random integer <code>[min, max]</code>. This method is
     * threadsafe.
     *
     * @param min Minimum size of the number, inclusive.
     * @param max Maximum size of the number, inclusive.
     * @return Random number between <code>min</code> and <code>max</code>.
     * @throws IllegalArgumentException If <code>min > max</code>.
     */
    public static int random(int min, int max) {
        if (min == max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Returns a random decimal <code>[min, max)</code>. This method is
     * threadsafe.
     *
     * @param min minimum size of the number
     * @param max maximum size of the number
     * @return random double between min and max
     */
    public static double random(double min, double max) {
        if (min == max) return min;
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Returns <code>true</code> if <code>chance</code> is greater than a
     * randomly generated decimal. If <code>chance</code> not between 0.0
     * (exclusive) and 1.0 (exclusive), then no random number is generated.
     *
     * @param chance A decimal between 0.0 (inclusive) and 1.0 (inclusive).
     * @return <code>true</code> If the chance was <i>successful</i>.
     */
    public static boolean chance(double chance) {
        if (chance <= 0.0) {
            return false;
        } else if (chance >= 1.0) {
            return true;
        } else {
            return ThreadLocalRandom.current().nextDouble() < chance;
        }
    }

    /**
     * Shorthand for using {@link Math#min(int, int)} and
     * {@link Math#max(int, int)}. The resulting number is less than or equal
     * to <code>max</code>, and greater than or equal to <code>min</code>.
     *
     * @param min   The minimum number <code>value</code> can be.
     * @param value The value of the number to check. This is the number that
     *              is probably between <code>min</code> and <code>max</code>.
     * @param max   The maximum number <code>value</code> can be.
     * @return A number <code>[min, max]</code> that is equal to one of the
     * method parameters.
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
     * Shorthand for using {@link Math#min(float, float)} and
     * {@link Math#max(float, float)}. The resulting number is less than or
     * equal to <code>max</code>, and greater than or equal to
     * <code>min</code>.
     *
     * @param min   The minimum number <code>value</code> can be.
     * @param value The value of the number to check. This is the number that
     *              is probably between <code>min</code> and <code>max</code>.
     * @param max   The maximum number <code>value</code> can be.
     * @return A number <code>[min, max]</code> that is equal to one of the
     * method parameters.
     */
    public static float minMax(float min, float value, float max) {
        if (min > value) {
            return min;
        } else if (max < value) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Shorthand for using {@link Math#min(double, double)} and
     * {@link Math#max(double, double)}. The resulting number is less than or
     * equal to <code>max</code>, and greater than or equal to
     * <code>min</code>.
     *
     * @param min   The minimum number <code>value</code> can be.
     * @param value The value of the number to check. This is the number that
     *              is probably between <code>min</code> and <code>max</code>.
     * @param max   The maximum number <code>value</code> can be.
     * @return A number <code>[min, max]</code> that is equal to one of the
     * method parameters.
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
     * Shorthand to square a given number. Avoids using <code>Math.pow</code>,
     * which wastes resources.
     *
     * @param num The number to square.
     * @return The result.
     */
    public static double square(double num) {
        return num * num;
    }

    /**
     * Returns <code>true</code> if the 2 given numbers are redundantly similar
     * (To ~10 decimal places) that they could be considered equal. This is
     * useful for math with floating point numbers, since binary has a hard
     * time properly representing certain decimals.
     *
     * @param a The first number.
     * @param b The second number.
     * @return <code>true</code> if the numbers are similar.
     */
    public static boolean equals(double a, double b) {
        return a == b || Math.abs(a - b) < 1e-6;
    }

    /**
     * <a href="https://en.wikipedia.org/wiki/Linear_interpolation">Linear interpolation</a>
     * implementation. The returned number will be between min inclusively, and
     * max inclusively.
     *
     * <p>The factor decides where, relative to <code>min</code> and
     * <code>max</code>, the returned point will be. The factor should be a
     * number between 0 inclusively and 1 inclusively. Values approaching 0.0
     * will return a point closer to <code>min</code>, while values approaching
     * 1.0 will return a point closer to <code>max</code>.
     *
     * @param min    The minimum value that the function can return.
     * @param max    The maximum value that the function can return.
     * @param factor At what point between the minimum and maximum should the
     *               returned value be.
     * @return The interpolated number.
     */
    public static double lerp(double min, double max, double factor) {
        if (factor < 0.0 || factor > 1.0)
            throw new IllegalArgumentException("Invalid factor: " + factor);

        return min + factor * (max - min);
    }

    /**
     * Returns a trimmed {@link String} holding the roman numeral
     * representation of the given number <code>from</code>.
     *
     * @param from The non-negative integer to translate to a roman numeral.
     * @return The non-null, trimmed string containing the roman numeral.
     */
    public static String toRomanNumeral(int from) {
        if (from <= 0)
            return "nulla"; // nulla, sometimes used for 0

        int numeral = NUMERALS.floorKey(from);
        if (from == numeral) {
            return NUMERALS.get(from);
        }
        return NUMERALS.get(numeral) + toRomanNumeral(from - numeral);
    }

    /**
     * Returns a {@link String} holding the time representation of the given
     * number of seconds.
     *
     * @param seconds The non-negative integer holding the amount of seconds.
     * @return The non-null human-readable time {@link String}.
     */
    public static String toTime(int seconds) {
        int unit = TIME.floorKey(seconds);
        int amount = seconds / unit;
        if (seconds % unit == 0) {
            return amount + TIME.get(unit);
        }
        return amount + TIME.get(unit) + " " + toTime(seconds - amount * unit);
    }

    /**
     * Returns <code>true</code> if an <code>amount</code> of time has passed
     * since <code>lastMillis</code> was saved.
     *
     * @param lastMillis The non-negative time at which the countdown started.
     *                   This number should be in milliseconds.
     * @param amount     The non-negative amount of time before returning
     *                   <code>true</code>. This number should be in
     *                   milliseconds.
     * @return <code>true</code> if enough time has passed.
     */
    public static boolean hasMillisPassed(long lastMillis, long amount) {
        return System.currentTimeMillis() - lastMillis > amount;
    }

    /**
     * Returns a rounded decimal to a given number of significant decimal
     * places.
     *
     * @param value        The decimal to round.
     * @param significands The non-negative number of significant digits.
     * @return The rounded decimal.
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
