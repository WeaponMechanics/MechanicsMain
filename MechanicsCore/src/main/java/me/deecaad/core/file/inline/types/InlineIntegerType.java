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
public class InlineIntegerType implements ArgumentType<Integer> {

    private final int min;
    private final int max;

    public InlineIntegerType() {
        this(Integer.MIN_VALUE);
    }

    public InlineIntegerType(int min) {
        this(min, Integer.MAX_VALUE);
    }

    public InlineIntegerType(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public Integer serialize(String str) throws InlineException {
        try {
            int temp = Integer.parseInt(str);
            if (temp < min || temp > max)
                throw new InlineException(str, new SerializerRangeException("", min, temp, max, ""));

            return temp;

        } catch (NumberFormatException ex) {
            throw new InlineException(str, new SerializerTypeException("", Integer.class, String.class, str, ""));
        }
    }
}
