package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.SerializerRangeException;
import me.deecaad.core.file.SerializerTypeException;
import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;

/**
 * This argument type parses an integer from config using
 * {@link Integer#parseInt(String)}. The integer will be clamped to the range
 * [{@link #getMin()}, {@link #getMax()}].
 *
 * TODO custom IntegerProvider allowing math, random, etc.
 */
public class DoubleType implements ArgumentType<Double> {

    private final double min;
    private final double max;

    public DoubleType() {
        this(Double.MIN_VALUE);
    }

    public DoubleType(double min) {
        this(min, Double.MAX_VALUE);
    }

    public DoubleType(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public Double serialize(String str) throws InlineException {
        try {
            double temp = Double.parseDouble(str);
            if (temp < min || temp > max)
                throw new InlineException(str, new SerializerRangeException("", min, temp, max, ""));

            return temp;

        } catch (NumberFormatException ex) {
            throw new InlineException(str, new SerializerTypeException("", Double.class, String.class, str, ""));
        }
    }

    @Override
    public String example() {
        return "0.5";
    }
}