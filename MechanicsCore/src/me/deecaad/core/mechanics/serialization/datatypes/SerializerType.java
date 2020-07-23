package me.deecaad.core.mechanics.serialization.datatypes;

import me.deecaad.core.mechanics.serialization.MechanicListSerializer;
import me.deecaad.core.mechanics.serialization.StringSerializable;
import me.deecaad.core.utils.ReflectionUtil;

import java.util.Map;

public class SerializerType<T extends StringSerializable<T>> extends DataType<StringSerializable<T>> {

    private Class<T> clazz;

    public SerializerType(Class<T> clazz) {
        super("Serializer");

        this.clazz = clazz;
    }

    @Override
    public StringSerializable<T> serialize(String str) {
        T t = ReflectionUtil.newInstance(clazz);

        Map<String, Object> data = MechanicListSerializer.getArguments(t.getName(), str, t.getArgs());
        return t.serialize(data);
    }

    @Override
    public boolean validate(String str) {
        return false;
    }
}
