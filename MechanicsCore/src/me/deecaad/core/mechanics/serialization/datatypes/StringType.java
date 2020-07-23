package me.deecaad.core.mechanics.serialization.datatypes;

public class StringType extends DataType<String> {

    public StringType() {
        super("String");
    }

    @Override
    public String serialize(String str) {
        return str;
    }

    @Override
    public boolean validate(String str) {
        return true;
    }
}
