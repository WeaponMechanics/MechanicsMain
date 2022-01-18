package me.deecaad.core.file;

import me.deecaad.core.utils.ReflectionUtil;

import javax.annotation.Nonnull;

public class SerializerVersionException extends SerializerException {

    public SerializerVersionException(@Nonnull String name, int minimumVersion, String feature, @Nonnull String location) {
        super(name, getMessages(minimumVersion, feature), location);
    }

    public SerializerVersionException(@Nonnull Serializer<?> serializer, int minimumVersion, String feature, @Nonnull String location) {
        super(serializer, getMessages(minimumVersion, feature), location);
    }

    private static String[] getMessages(int minimumVersion, String feature) {
        return new String[] {
                "Sorry, but " + feature + " is not supported on MC Version 1." + ReflectionUtil.getMCVersion(),
                "Please upgrade your server to 1." + minimumVersion + " to use this feature"
        };
    }
}
