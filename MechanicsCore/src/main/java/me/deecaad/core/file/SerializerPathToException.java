package me.deecaad.core.file;

public class SerializerPathToException extends SerializerException {

    public final Serializer<?> serializer;
    public final SerializeData data;
    public final String storeAt;
    public final String pullFrom;

    public SerializerPathToException(Serializer<?> serializer, SerializeData data) {
        super(data.serializer, getMessages(serializer, data), data.of().getLocation());

        this.serializer = serializer;
        this.data = data;
        this.storeAt = data.key;
        this.pullFrom = data.of().get(null);

        if (this.pullFrom == null)
            throw new IllegalStateException("Something went wrong...");
    }

    private static String[] getMessages(Serializer<?> serializer, SerializeData data) {
        return new String[] {
                "Attempted to use 'Path_To' feature when the plugin (or serializer) doesn't support it!",
                "'Path_To' is used so server admins can re-use their config without copying and pasting.",
                "Found path: " + data.of().get("-Unknown-"),
                "Serializer Keyword: " + serializer.getKeyword()
        };
    }
}
