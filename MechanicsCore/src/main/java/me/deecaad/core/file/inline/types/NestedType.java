package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.inline.InlineSerializer;
import me.deecaad.core.utils.ReflectionUtil;

import java.lang.reflect.Modifier;
import java.util.stream.Collectors;

/**
 * NestedTypes are great for when you want to nest a <b>KNOWN</b> type in your
 * inline serializer. For example, if you wanted to nest an item in an entity:
 *
 * <blockquote><pre>{@code
 *  - entity(helditem=item(DIRT))
 * }</pre></blockquote>
 *
 * <p>When the precise type is not known, you are probably looking to use a
 * {@link RegistryType}.
 *
 * @param <T> The type of the serializer.
 */
public class NestedType<T extends InlineSerializer<T>> implements ArgumentType<T> {

    private final T serializer;

    public NestedType(T serializer) {
        if (Modifier.isAbstract(serializer.getClass().getModifiers()))
            throw new IllegalArgumentException("Cannot use abstract class for NestedType");
        this.serializer = serializer;
    }

    public NestedType(Class<T> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers()))
                throw new IllegalArgumentException("Cannot use abstract class for NestedType");
        this.serializer = ReflectionUtil.newInstance(clazz);
    }

    public T getSerializer() {
        return serializer;
    }

    @Override
    public T serialize(String str) throws InlineException {
        throw new UnsupportedOperationException("Cannot call serialize(String) on NestedType");
    }

    @Override
    public String example() {
        return serializer.getKeyword() + "(" + serializer.args().getArgs().values().stream()
                .map(arg -> arg.getName() + "=" + arg.getType().example()).collect(Collectors.joining(", ")) + ")";
    }
}
