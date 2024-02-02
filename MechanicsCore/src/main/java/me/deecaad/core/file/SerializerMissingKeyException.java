package me.deecaad.core.file;

import org.jetbrains.annotations.NotNull;

public class SerializerMissingKeyException extends SerializerException {

    public SerializerMissingKeyException(@NotNull String name, String key, @NotNull String location) {
        super(name, getMessages(key), location);
    }

    public SerializerMissingKeyException(@NotNull Serializer<?> serializer, String key, @NotNull String location) {
        super(serializer, getMessages(key), location);
    }

    private static String[] getMessages(String key) {
        return new String[]{
                "You are missing the required config option '" + key + "'",
                "Make sure that your configuration is spelled correctly (It is case-sensitive!)"
        };
    }
}
