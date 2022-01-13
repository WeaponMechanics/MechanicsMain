package me.deecaad.core.file;

import org.jetbrains.annotations.NotNull;

public class SerializerRangeException extends SerializerException {

    public SerializerRangeException(@NotNull String name, int min, int actual, int max, @NotNull String location) {
        super(name, getMessages(min, actual, max), location);
    }

    public SerializerRangeException(@NotNull Serializer<?> serializer, int min, int actual, int max, @NotNull String location) {
        super(serializer, getMessages(min, actual, max), location);
    }

    public SerializerRangeException(@NotNull String name, double min, double actual, double max, @NotNull String location) {
        super(name, getMessages(min, actual, max), location);
    }

    public SerializerRangeException(@NotNull Serializer<?> serializer, double min, double actual, double max, @NotNull String location) {
        super(serializer, getMessages(min, actual, max), location);
    }

    private static String[] getMessages(int min, int actual, int max) {
        return new String[] {
                "Given integer needs to be between " + min + " and " + max,
                forValue(actual)
        };
    }

    private static String[] getMessages(double min, double actual, double max) {
        return new String[] {
                "Given integer needs to be between " + min + " and " + max,
                forValue(actual)
        };
    }
}
