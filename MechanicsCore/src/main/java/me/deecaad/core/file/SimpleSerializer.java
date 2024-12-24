package me.deecaad.core.file;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A serializer that can parse objects from strings. The serializer is "simple" because the string
 * is expected in a human-readable format. This means no JSON, XML, or other complex formats. A
 * value more like "DIRT" or "5" is expected.
 *
 * <p>
 * This is a type of {@link Serializer}, but it only works on strings.
 *
 * @param <T> The type of object to parse.
 */
public interface SimpleSerializer<T> extends Serializer<T> {

    /**
     * The name of the type of the serializer. This is used for error messages.
     */
    @NotNull String getTypeName();

    /**
     * Parses an object from a string.
     *
     * @param data The string to parse.
     * @param errorLocation The location of the error in the file.
     * @return The parsed object.
     */
    @NotNull T deserialize(@NotNull String data, @NotNull String errorLocation) throws SerializerException;

    @Override
    default @NotNull T serialize(@NotNull SerializeData data) throws SerializerException {
        // SimpleSerializers expect only one string value
        if (data.of().is(List.class)) {
            throw SerializerException.builder()
                .locationRaw(data.of().getLocation())
                .addMessage("Expected a single " + getTypeName() + ", but found a list")
                .build();
        }

        String value = data.of().assertExists().get(Object.class).get().toString();
        return deserialize(value, data.of().getLocation());
    }
}
