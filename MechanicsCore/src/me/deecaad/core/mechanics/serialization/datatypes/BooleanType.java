package me.deecaad.core.mechanics.serialization.datatypes;

/**
 * <code>true</code> or <code>false</code>. Ignores case
 */
public class BooleanType extends DataType<Boolean> {

    BooleanType() {
        super("BOOLEAN");
    }

    @Override
    public Boolean serialize(String str) {
        return Boolean.valueOf(str);
    }

    @Override
    public boolean validate(String str) {
        String lower = str.toLowerCase();

        return lower.equals("true") || lower.equals("false");
    }
}
