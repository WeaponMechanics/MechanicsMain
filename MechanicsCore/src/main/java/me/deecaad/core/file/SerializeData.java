package me.deecaad.core.file;

import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.io.File;

public class SerializeData {

    public final Serializer<?> serializer;
    public final File file;
    public final String key;
    public final ConfigurationSection config;

    public SerializeData(@Nonnull Serializer<?> serializer, @Nonnull File file, @Nonnull String key, @Nonnull ConfigurationSection config) {
        this.serializer = serializer;
        this.file = file;
        this.key = key;
        this.config = config;
    }

    public SerializeData(@Nonnull Serializer<?> serializer, @Nonnull SerializeData other, @Nonnull String relative) {
        this.serializer = serializer;
        this.file = other.file;
        this.key = other.key;
        ConfigurationSection temp = other.config.getConfigurationSection(relative);

        if (temp == null)
            throw new IllegalArgumentException("No config section at " + relative);
        else
            this.config = temp;
    }

    public ConfigAccessor of(String relative) {
        return new ConfigAccessor(relative);
    }

    public class ConfigAccessor {

        private final String relative;

        private ConfigAccessor(String relative) {
            this.relative = relative;
        }

        public ConfigAccessor assertExists() throws SerializerException {
            if (!config.contains(relative, true)) {
                throw new SerializerMissingKeyException(serializer, relative, relative);
            }

            return this;
        }

        public ConfigAccessor assertType(Class<?> type) throws SerializerException {
            Object value = config.get(relative);

            // Use assertExists for required keys
            if (value != null) {
                Class<?> actual = value.getClass();
                if (!type.isAssignableFrom(actual)) {
                    throw new SerializerTypeException(serializer, type, actual, value, getLocation());
                }
            }

            return this;
        }

        public ConfigAccessor assertNumber() throws SerializerException {
            Object value = config.get(relative);

            // Use assertExists for required keys
            if (value == null)
                return this;

            try {

                // Using a double is not perfect since somebody may use a Long
                // in config, which may cause issues. For int/float/double, this
                // should work without issue.
                double ignore = (double) value;

            } catch (ClassCastException ex) {

                // assertPositive is also effective for asserting the given type is a number.
                throw new SerializerTypeException(serializer, Number.class, value.getClass(), value, getLocation());
            }

            return this;
        }

        public ConfigAccessor assertPositive() throws SerializerException {
            Object value = config.get(relative);

            // Use assertExists for required keys
            if (value == null)
                return this;

            try {

                // Using a double is not perfect since somebody may use a Long
                // in config, which may cause issues. For int/float/double, this
                // should work without issue.
                double num = (double) value;
                if (num < 0.0)
                    throw new SerializerNegativeException(serializer, num, getLocation());

            } catch (ClassCastException ex) {

                // assertPositive is also effective for asserting the given type is a number.
                throw new SerializerTypeException(serializer, Number.class, value.getClass(), value, getLocation());
            }

            return this;
        }

        public ConfigAccessor assertRange(int min, int max) throws SerializerException {
            Object value = config.get(relative);

            // Use assertExists for required keys
            if (value == null)
                return this;

            try {

                // Silently strips away float point data (without exception)
                int num = (int) value;
                if (num < min || num > max)
                    throw new SerializerRangeException(serializer, min, num, max, getLocation());

            } catch (ClassCastException ex) {
                throw new SerializerTypeException(serializer, Integer.class, value.getClass(), value, getLocation());
            }

            return this;
        }

        public ConfigAccessor assertRange(double min, double max) throws SerializerException {
            Object value = config.get(relative);

            // Use assertExists for required keys
            if (value == null)
                return this;

            try {

                // Silently strips away float point data (without exception)
                double num = (double) value;
                if (num < min || num > max)
                    throw new SerializerRangeException(serializer, min, num, max, getLocation());

            } catch (ClassCastException ex) {
                throw new SerializerTypeException(serializer, Double.class, value.getClass(), value, getLocation());
            }

            return this;
        }

        public <T extends Serializer<T>> T serialize(Class<T> serializerClass) throws SerializerException {
            T serializer = ReflectionUtil.newInstance(serializerClass);
            SerializeData data = new SerializeData(serializer, SerializeData.this, relative);
            return serializer.serialize(data);
        }

        private String getLocation() {
            if (relative == null || "".equals(relative)) {
                return StringUtil.foundAt(file, key);
            } else {
                return StringUtil.foundAt(file, key + "." + relative);
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T get() {
            return (T) config.get(relative);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(T defaultValue) {
            return (T) config.get(relative, defaultValue);
        }
    }
}
