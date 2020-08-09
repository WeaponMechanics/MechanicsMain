package me.deecaad.core.mechanics.serialization.datatypes;

import java.util.regex.Pattern;

/**
 * Matches any positive or negative <code>Integer</code>
 */
public class IntegerType extends DataType<Integer> {

    private static final Pattern PATTERN = Pattern.compile("-?\\d+");

    public IntegerType() {
        super("INTEGER");
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
