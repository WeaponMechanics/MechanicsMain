package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;

import java.util.List;

public class ListType<E, T extends ArgumentType<E>> implements ArgumentType<List<E>> {

    private final T type;
    private final int minArgs;

    public ListType(T type) {
        this(type, 0);
    }

    public ListType(T type, int minArgs) {
        this.type = type;
        this.minArgs = minArgs;

        if (type instanceof EnumType<?> enumType && !enumType.isAllowWildcard())
            throw new IllegalArgumentException("Hello developer! Make sure EnumType#isAllowedWildcard() is true for lists");
    }

    public T getType() {
        return type;
    }

    public int getMinArgs() {
        return minArgs;
    }

    @Override
    public List<E> serialize(String str) throws InlineException {
        throw new UnsupportedOperationException("Cannot call serialize(String) on ListType");
    }

    @SuppressWarnings("unchecked")
    public List<E> serializeOne(String str) throws InlineException {
        if (type instanceof EnumType enumType)
            return enumType.serializeList(str);
        else
            return List.of(type.serialize(str));
    }

    @Override
    public String example() {
        return "[" + type.example() + ", " + type.example() + "]";
    }

    @Override
    public boolean isComplex() {
        return true;
    }
}
