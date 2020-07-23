package me.deecaad.core.mechanics.serialization.datatypes;

import java.util.regex.Pattern;

public class DoubleType extends DataType<Double> {

    private static final Pattern PATTERN = Pattern.compile("\\d*\\.?\\d+");

    public DoubleType() {
        super("Double");
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
