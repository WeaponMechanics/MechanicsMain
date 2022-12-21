package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.inline.InlineSerializer;
import me.deecaad.core.utils.ReflectionUtil;

public class InlineSerializerType<T extends InlineSerializer<T>> implements ArgumentType<T> {

    private final T serializer;

    public InlineSerializerType(T serializer) {
        this.serializer = serializer;
    }

    public InlineSerializerType(Class<T> clazz) {
        this.serializer = ReflectionUtil.newInstance(clazz);
    }

    public T getSerializer() {
        return serializer;
    }

    @Override
    public T serialize(String str) throws InlineException {
        throw new UnsupportedOperationException("Cannot call serialize(String) on InlineSerializerType");
    }
}
