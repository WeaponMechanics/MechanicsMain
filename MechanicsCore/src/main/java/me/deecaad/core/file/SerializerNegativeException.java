package me.deecaad.core.file;

import org.jetbrains.annotations.NotNull;

public class SerializerNegativeException extends SerializerException {

    public SerializerNegativeException(@NotNull String name, Object value, @NotNull String location) {
        super(name, getMessages(value), location);
    }

    public SerializerNegativeException(@NotNull Serializer<?> serializer, Object value, @NotNull String location) {
        super(serializer, getMessages(value), location);
    }

    private static String[] getMessages(Object value) {
        return new String[]{
                "Expected a positive number, but got a negative number",
                forValue(value)
        };
    }
}
