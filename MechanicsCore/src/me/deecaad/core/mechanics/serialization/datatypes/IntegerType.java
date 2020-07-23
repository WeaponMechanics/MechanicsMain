package me.deecaad.core.mechanics.serialization.datatypes;

import java.util.regex.Pattern;

public class IntegerType extends DataType<Integer> {

    private static final Pattern PATTERN = Pattern.compile("\\d+");

    public IntegerType() {
        super("Integer");
    }

    @Override
    public Integer serialize(String str) {

        // Do not use Integer.parseInt (avoid boxing then unboxing)
        return Integer.valueOf(str);
    }

    @Override
    public boolean validate(String str) {
        return PATTERN.matcher(str).matches();
    }
}
