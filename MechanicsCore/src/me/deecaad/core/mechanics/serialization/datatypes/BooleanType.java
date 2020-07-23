package me.deecaad.core.mechanics.serialization.datatypes;

public class BooleanType extends DataType<Boolean> {

    BooleanType() {
        super("Boolean");
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
