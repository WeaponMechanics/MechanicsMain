package me.deecaad.core.commands.wrappers;

/**
 * Immutable class wrapping a minimum bound and maximum bound.
 */
public class DoubleRange {

    private final double min;
    private final double max;

    public DoubleRange(double min, double max) {
        if (Double.isNaN(min) || Double.isNaN(max))
            throw new NumberFormatException(min + ".." + max + " is not a valid range! Use real numbers!");

        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public double getMin() {
        return min;
    }

    public DoubleRange setMin(double min) {
        return new DoubleRange(min, max);
    }

    public double getMax() {
        return max;
    }

    public DoubleRange setMax(double max) {
        return new DoubleRange(min, max);
    }

    public boolean inRange(double point) {
        return point >= min && point <= max;
    }
}
