package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.inline.InlineSerializer;
import me.deecaad.core.utils.ReflectionUtil;

import java.util.stream.Collectors;

public class NestedType<T extends InlineSerializer<T>> implements ArgumentType<T> {

    private final T serializer;

    public NestedType(T serializer) {
        this.serializer = serializer;
    }

    public NestedType(Class<T> clazz) {
        this.serializer = ReflectionUtil.newInstance(clazz);
    }

    public T getSerializer() {
        return serializer;
    }

    @Override
    public T serialize(String str) throws InlineException {
        throw new UnsupportedOperationException("Cannot call serialize(String) on InlineSerializerType");
    }

    @Override
    public String example() {
        return serializer.getKeyword() + "(" + serializer.args().getArgs().values().stream()
                .map(arg -> arg.getName() + "=" + arg.getType().example()).collect(Collectors.joining(", ")) + ")";
    }
}
