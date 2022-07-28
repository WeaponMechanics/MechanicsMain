package me.deecaad.core.commands.wrappers;

/**
 * Immutable class wrapping a minimum and maximum bound.
 */
public class IntRange {

    private final int min;
    private final int max;

    public IntRange(int min, int max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public int getMin() {
        return min;
    }

    public IntRange setMin(int min) {
        return new IntRange(min, max);
    }

    public int getMax() {
        return max;
    }

    public IntRange setMax(int max) {
        return new IntRange(min, max);
    }

    public boolean inRange(int point) {
        return point >= min && point <= max;
    }
}
