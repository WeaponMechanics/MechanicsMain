package me.deecaad.core.file;

import org.jetbrains.annotations.NotNull;

public class SerializerTypeException extends SerializerException {

    public SerializerTypeException(@NotNull Serializer<?> serializer, Class<?> expected,
                                   Class<?> actual, Object value, @NotNull String location) {

        super(serializer, getMessages(expected, actual, value), location);
    }

    private static String[] getMessages(Class<?> expected, Class<?> actual, Object value) {
        return new String[] {
                "Expected a(n) " + expected.getSimpleName() + ", but got a(n) " + (actual == null ? "Unknown Type" : actual.getSimpleName()),
                forValue(value)
        };
    }
}
