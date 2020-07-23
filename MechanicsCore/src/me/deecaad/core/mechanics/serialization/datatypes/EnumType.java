package me.deecaad.core.mechanics.serialization.datatypes;

import com.google.common.base.Enums;

public class EnumType<T extends Enum<T>> extends DataType<T> {

    private final Class<T> clazz;

    public EnumType(Class<T> clazz) {
        super("Entity");

        this.clazz = clazz;
    }

    @Override
    public T serialize(String str) {
        return Enum.valueOf(clazz, str.trim().toUpperCase());
    }

    @Override
    public boolean validate(String str) {
        return Enums.getIfPresent(clazz, str.trim().toUpperCase()).isPresent();
    }
}
