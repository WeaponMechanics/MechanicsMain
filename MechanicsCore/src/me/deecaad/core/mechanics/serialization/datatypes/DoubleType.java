package me.deecaad.core.mechanics.serialization.datatypes;

import java.util.regex.Pattern;

/**
 * Matches any positive or negative <code>Number</code>.
 * Examples: 0.0, 0, -30.9, 10.00000001
 */
public class DoubleType extends DataType<Double> {

    private static final Pattern PATTERN = Pattern.compile("-?\\d*\\.?\\d+");

    public DoubleType() {
        super("DOUBLE");
    }

    @Override
    public Double serialize(String str) {
        return Double.valueOf(str);
    }

    @Override
    public boolean validate(String str) {
        return PATTERN.matcher(str).matches();
    }
}
