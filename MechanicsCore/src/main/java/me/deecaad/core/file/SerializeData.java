package me.deecaad.core.file;

import me.deecaad.core.utils.StringUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public class SerializeData {

    public final Serializer<?> serializer;
    public final File file;
    public final String key;
    public final ConfigurationSection config;

    public SerializeData(Serializer<?> serializer, File file, String key, ConfigurationSection config) {
        this.serializer = serializer;
        this.file = file;
        this.key = key;
        this.config = config;
    }

    public void assertExists(String relative) throws SerializerException {
        if (!config.contains(relative, true)) {
            throw new SerializerMissingKeyException(serializer, relative, relative);
        }
    }

    public void assertType(String relative, Class<?> type) throws SerializerException {
        Object value = config.get(relative);

        // Use assertExists for required keys
        if (value == null)
            return;

        Class<?> actual = value.getClass();
        if (!type.isAssignableFrom(actual)) {
            throw new SerializerTypeException(serializer, type, actual, value, getLocation(relative));
        }
    }

    public void assertPositive(String relative) throws SerializerException {
        Object value = config.get(relative);

        // Use assertExists for required keys
        if (value == null)
            return;

        try {

            // Using a double is not perfect since somebody may use a Long
            // in config, which may cause issues. For int/float/double, this
            // should work without issue.
            double num = (double) value;
            if (num < 0.0)
                throw new SerializerNegativeException(serializer, num, getLocation(relative));

        } catch (ClassCastException ex) {

            // assertPositive is also effective for asserting the given type is a number.
            throw new SerializerTypeException(serializer, Number.class, value.getClass(), value, getLocation(relative));
        }
    }

    public void assertRange(String relative, int min, int max) throws SerializerException {
        Object value = config.get(relative);

        // Use assertExists for required keys
        if (value == null)
            return;

        try {

            // Silently strips away float point data (without exception)
            int num = (int) value;
            if (num < min || num > max)
                throw new SerializerRangeException(serializer, min, num, max, getLocation(relative));

        } catch (ClassCastException ex) {
            throw new SerializerTypeException(serializer, Integer.class, value.getClass(), value, getLocation(relative));
        }
    }

    public void assertRange(String relative, double min, double max) throws SerializerException {
        Object value = config.get(relative);

        // Use assertExists for required keys
        if (value == null)
            return;

        try {

            // Silently strips away float point data (without exception)
            double num = (double) value;
            if (num < min || num > max)
                throw new SerializerRangeException(serializer, min, num, max, getLocation(relative));

        } catch (ClassCastException ex) {
            throw new SerializerTypeException(serializer, Double.class, value.getClass(), value, getLocation(relative));
        }
    }

    public String getLocation(String relative) {
        if (relative == null || "".equals(relative)) {
            return StringUtil.foundAt(file, key);
        } else {
            return StringUtil.foundAt(file, key + "." + relative);
        }
    }
}
