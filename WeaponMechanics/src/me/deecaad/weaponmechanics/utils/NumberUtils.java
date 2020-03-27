package me.deecaad.weaponmechanics.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class NumberUtils {
    
    /**
     * Don't let anyone instantiate this class
     */
    private NumberUtils() {
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
    public static double random(double min, double max) {
        if (min == max) return min;
        return ThreadLocalRandom.current().nextDouble(min, max);
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
