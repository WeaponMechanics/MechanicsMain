package me.deecaad.core.file;

import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Wraps a key (<i>Usually</i> pointing to a {@link ConfigurationSection}) with
 * serializer functions to help ensure valid config. The key will not point to
 * a configuration section when the internal {@link Serializer} does not use
 * a configuration section (or when the configuration writer did something
 * very wrong).
 */
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
        this.key = other.key + "." + relative;
        ConfigurationSection temp = other.config.getConfigurationSection(relative);

        if (temp == null)
            throw new IllegalArgumentException("No config section at " + relative);
        else
            this.config = temp;
    }

    /**
     * Helper method to "move" into a new configuration section. The given
     * relative key should <i>always</i> point towards a
     * {@link ConfigurationSection}
     *
     * @param relative The non-null, non-empty key relative to this.key.
     * @return The non-null serialize data.
     * @throws IllegalArgumentException If no configuration section exists at the location.
     */
    public SerializeData move(String relative) {
        return new SerializeData(serializer, this, relative);
    }

    /**
     * Helper method to "move out" into the parent section.
     *
     * @return The non-null serialize data.
     */
    public SerializeData out() {
        String[] split = key.split("\\.");
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < split.length - 2; i++)
            key.append(split[i]);

        return new SerializeData(serializer, file, key.toString(), config);
    }

    public ConfigAccessor of() {
        String[] split = key.split("\\.");
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < split.length - 2; i++)
            key.append(split[i]);

        return new SerializeData(serializer, file, key.toString(), config).of(split[split.length - 1]);
    }

    /**
     * Creates a {@link ConfigAccessor} which accesses the data (stored in
     * config) at <code>this.key + "." + relative</code>. The returned accessor
     * can be used to validate arguments.
     *
     * @param relative The non-null, non-empty key relative to this.key.
     * @return The non-null config accessor.
     */
    public ConfigAccessor of(String relative) {
        return new ConfigAccessor(relative);
    }

    /**
     * Creates a {@link ConfigListAccessor} which accesses the data (stored in
     * config) at <code>this.key + ".' + relative</code>. The returned accessor
     * can be used to validate arguments.
     *
     * @param relative The non-null, non-empty key relative to this.key.
     * @return The non-null config list accessor.
     */
    public ConfigListAccessor ofList(String relative) {
        return new ConfigListAccessor(relative);
    }

    /**
     * When there is no method in {@link ConfigAccessor} to match a specific
     * configuration error, you may check for it manually and use this method
     * to throw an exception.
     *
     * <p>Make sure to keep messages clear and concise. There is no limit to
     * how many messages you may give to the player, but make sure that each
     * message is <i>important</i> and contains <i>useful</i> information.
     *
     * @param relative The nullable relative key.
     * @param messages The non-null list of messages to include.
     * @throws SerializerException Always throws an exception... That's the point :p
     */
    public void throwException(String relative, String... messages) throws SerializerException {
        String key = this.key + ((relative == null || relative.isEmpty()) ? "" : "." + relative);
        throw new SerializerException(serializer, messages, StringUtil.foundAt(file, key));
    }

    public void throwListException(String relative, int index, String... messages) throws SerializerException {
        String key = this.key + ((relative == null || relative.isEmpty()) ? "" : "." + relative);
        throw new SerializerException(serializer, messages, StringUtil.foundAt(file, key, index + 1));
    }

    public class ConfigListAccessor {

        // Stores the class arguments, which is used to check the format
        private final LinkedList<ClassArgument> arguments;
        private final String relative;

        public ConfigListAccessor(String relative) {
            this.arguments = new LinkedList<>();
            this.relative = relative;
        }

        public ConfigListAccessor addArgument(Class<?> clazz, boolean required) {
            return this.addArgument(clazz, required, false);
        }

        public ConfigListAccessor addArgument(Class<?> clazz, boolean required, boolean skipCheck) {

            // Ensure that all required arguments are in order. For example,
            // true~true~false is fine, but true~false~true is impossible to
            // serialize.
            if (required && arguments.getLast() != null && !arguments.getLast().required)
                throw new IllegalArgumentException("Required arguments must be consecutive");

            ClassArgument arg = new ClassArgument();
            arg.clazz = clazz;
            arg.required = required;
            arg.skipCheck = skipCheck;
            arguments.add(arg);
            return this;
        }

        /**
         * Asserts that this key exists in the configuration. This method
         * ensures that the user explicitly defined a value for the key.
         *
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the key is not explicitly defined.
         */
        @Nonnull
        public ConfigListAccessor assertExists() throws SerializerException {
            if (!config.contains(key + "." + relative, true))
                throw new SerializerMissingKeyException(serializer, relative, getLocation());

            return this;
        }

        @Nonnull
        @SuppressWarnings({"unchecked", "rawtypes"})
        public ConfigListAccessor assertList() throws SerializerException {
            if (arguments.isEmpty())
                throw new IllegalStateException("Need to set arguments before assertions");

            // The first step is to assert that the value stored at this key
            // is a list (of any generic-type).
            Object value = config.get(key + "." + relative);
            if (value instanceof List)
                throw new SerializerTypeException(serializer, List.class, value.getClass(), value, getLocation());
            List<?> list = (List<?>) config.get(key + "." + relative);

            // Use assertExists for required keys
            if (list == null || list.isEmpty())
                return this;

            for (int i = 0; i < list.size(); i++) {
                String string = list.get(i).toString();

                // Show the user the correct format
                StringBuilder format = new StringBuilder("<");
                arguments.forEach(arg -> format.append(arg.clazz.getSimpleName()).append(">-<"));
                format.append('>');

                // Empty string in config is probably a mistake (Perhaps they
                // forgot to save?). Instead of ignoring this, we should tell
                // the user (playing it safe).
                if (string == null || string.trim().isEmpty()) {
                    throwListException(relative, i, relative + " does not allow empty elements in the list.",
                            "Valid Format: " + format);
                }

                // We expect each value to be a string in format like:
                // <String>~<Integer>~<Boolean>
                String[] split = StringUtil.split(string);

                // Missing required data
                int required = (int) arguments.stream().filter(arg -> arg.required).count();
                if (split.length < required) {
                    throwListException(relative, i, relative + " requires the first " + required + " arguments to be defined.",
                            SerializerException.forValue(string),
                            "You are missing " + (required - split.length) + " arguments",
                            "Valid Format: " + format
                    );
                }

                for (int j = 0; j < split.length; j++) {

                    // Extra data check. This happens when the user adds more
                    // data than what the list can take. For example, if this
                    // list uses the format 'string-int' and the user inputs
                    // 'string-int-double', then this will be triggered.
                    if (arguments.size() <= j) {
                        throwException("Invalid list format, " + relative + " can only use " + arguments.size() + " arguments.",
                                SerializerException.forValue(string),
                                "Valid Format: " + format
                        );
                    }

                    String component = split[j];
                    ClassArgument argument = arguments.get(j);
                    if (argument.skipCheck)
                        continue;

                    try {
                        if (argument.clazz == int.class) {
                            argument.clazz = Integer.class; // Set class to be more human-readable in error
                            Integer.parseInt(component);
                        } else if (argument.clazz == double.class) {
                            argument.clazz = Double.class;
                            Double.parseDouble(component);
                        } else if (argument.clazz == boolean.class) {
                            argument.clazz = Boolean.class;
                            if (!component.equalsIgnoreCase("true") && !component.equalsIgnoreCase("false"))
                                throw new Exception();
                        } else if (argument.clazz.isEnum() && EnumUtil.parseEnums((Class<Enum>) argument.clazz, component).isEmpty()) {
                            throw new SerializerEnumException(serializer, (Class<Enum>) argument.clazz, component, true, getLocation(i))
                                    .addMessage("Full List Element: " + string)
                                    .addMessage("Valid List Format: " + format);
                        }
                    } catch (SerializerException ex) {
                        throw ex; // Rethrow exception so it isn't caught and ignored
                    } catch (Exception ex) {
                        throw new SerializerTypeException(serializer, argument.clazz, null, component, getLocation(i))
                                .addMessage("Full List Element: " + string)
                                .addMessage("Valid List Format: " + format);
                    }
                }
            }

            return this;
        }

        @SuppressWarnings("rawtypes")
        public List<String[]> get() {

            // Use assertExists for required keys
            if (!config.contains(key + "." + relative))
                return Collections.emptyList();

            List<String[]> list = new ArrayList<>();
            for (Object obj : (List) config.get(key + "." + relative, Collections.emptyList())) {
                list.add(StringUtil.split(obj.toString()));
            }

            return list;
        }

        private String getLocation() {
            if (relative == null || "".equals(relative)) {
                return StringUtil.foundAt(file, key);
            } else {
                return StringUtil.foundAt(file, key + "." + relative);
            }
        }

        private String getLocation(int index) {
            if (relative == null || "".equals(relative)) {
                return StringUtil.foundAt(file, key, index);
            } else {
                return StringUtil.foundAt(file, key + "." + relative, index);
            }
        }

        private class ClassArgument {
            Class<?> clazz;
            boolean required;
            boolean skipCheck;
        }
    }

    /**
     * Wraps a configuration KEY to some helper functions to facilitate data
     * serialization. The (public) methods of this class will throw a
     * {@link SerializerException} if the configuration is invalid.
     *
     * <p>The methods of this class follow the Builder pattern.
     */
    public class ConfigAccessor {

        protected final String relative;

        private ConfigAccessor(String relative) {
            this.relative = relative;
        }

        /**
         * Asserts that this key exists in the configuration. This method
         * ensures that the user explicitly defined a value for the key.
         *
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the key is not explicitly defined.
         */
        @Nonnull
        public ConfigAccessor assertExists() throws SerializerException {
            if (!config.contains(key + "." + relative, true))
                throw new SerializerMissingKeyException(serializer, relative, getLocation());

            return this;
        }

        /**
         * Asserts that the value at this key is an instance of the given
         * class. Ensures that the datatype matches what the developer
         * expected the user to give.
         *
         * @param type The non-null data type to match.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the type does not match.
         */
        @Nonnull
        public ConfigAccessor assertType(Class<?> type) throws SerializerException {
            Object value = config.get(key + "." + relative);

            // Use assertExists for required keys
            if (value != null) {
                Class<?> actual = value.getClass();
                if (!type.isAssignableFrom(actual)) {
                    throw new SerializerTypeException(serializer, type, actual, value, getLocation());
                }
            }

            return this;
        }

        /**
         * Asserts that the value at this key is a number of any type. The
         * check is done by checking the value can be type-casted to a double.
         * Note that if you want a more specific number type (for example, an
         * integer), you should use {@link #assertType(Class)}.
         *
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the type is not a number.
         */
        @Nonnull
        public ConfigAccessor assertNumber() throws SerializerException {
            Object value = config.get(key + "." + relative);

            // Use assertExists for required keys
            if (value == null)
                return this;

            try {

                // Any number (long, short, int, byte, etc) can be type-casted
                // to a double in java.
                double ignore = (double) value;

            } catch (ClassCastException ex) {

                // assertPositive is also effective for asserting the given type is a number.
                throw new SerializerTypeException(serializer, Number.class, value.getClass(), value, getLocation());
            }

            return this;
        }

        /**
         * Asserts that the value at this key is a number, AND that the number
         * is positive. Note that if you want a more specific number type (for
         * example, an integer), you should use {@link #assertType(Class)}.
         *
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the type is not a number or is not positive.
         */
        @Nonnull
        public ConfigAccessor assertPositive() throws SerializerException {
            Object value = config.get(key + "." + relative);

            // Use assertExists for required keys
            if (value == null)
                return this;

            try {

                // We need to check longs first since they might have weird
                // behavior when we cast them to a double (data loss or
                // otherwise)
                if (value instanceof Long && ((Long) value) < 0L)
                    throw new SerializerNegativeException(serializer, value, getLocation());

                double num = (double) value;
                if (num < 0.0)
                    throw new SerializerNegativeException(serializer, num, getLocation());

            } catch (ClassCastException ex) {

                // assertPositive is also effective for asserting the given type is a number.
                throw new SerializerTypeException(serializer, Number.class, value.getClass(), value, getLocation());
            }

            return this;
        }

        /**
         * Asserts that the value at this key is a number, AND that the number
         * is within the inclusive range. Note that if you want a more specific
         * number type (for example, an integer), you should use
         * {@link #assertType(Class)}.
         *
         * @param min Inclusive minimum bound. min < max.
         * @param max Inclusive maximum bound. max > min.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the value is not in range.
         * @throws IllegalArgumentException If min > max.
         */
        @Nonnull
        public ConfigAccessor assertRange(int min, int max) throws SerializerException {
            if (min > max)
                throw new IllegalArgumentException("min > max");

            Object value = config.get(key + "." + relative);

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

        /**
         * Asserts that the value at this key is a number, AND that the number
         * is within the inclusive range. Note that if you want a more specific
         * number type (for example, an integer), you should use
         * {@link #assertType(Class)}.
         *
         * @param min Inclusive minimum bound. min < max.
         * @param max Inclusive maximum bound. max > min.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the value is not in range.
         * @throws IllegalArgumentException If min > max.
         */
        @Nonnull
        public ConfigAccessor assertRange(double min, double max) throws SerializerException {
            Object value = config.get(key + "." + relative);

            // Use assertExists for required keys
            if (value == null)
                return this;

            try {
                double num = (double) value;
                if (num < min || num > max)
                    throw new SerializerRangeException(serializer, min, num, max, getLocation());

            } catch (ClassCastException ex) {
                throw new SerializerTypeException(serializer, Double.class, value.getClass(), value, getLocation());
            }

            return this;
        }

        public String getLocation() {
            if (relative == null || "".equals(relative)) {
                return StringUtil.foundAt(file, key);
            } else {
                return StringUtil.foundAt(file, key + "." + relative);
            }
        }

        /**
         * Gets the data stored at this relative key. Note that this method
         * (basically) requires a previous call to {@link #assertExists()},
         * especially for primitive types. When the key is optional, use
         * {@link #get(Object)} to define a default value.
         *
         * @param <T> The expected data-type of the data.
         * @return The data stored at this relative key.
         */
        @SuppressWarnings("unchecked")
        public <T> T get() {
            return (T) config.get(key + "." + relative);
        }

        /**
         * Gets the data stored at this relative key, or
         * <code>defaultValue</code> if the key is not explicitly defined. It
         * does not make sense to use this method when there has been a
         * previous call to {@link #assertExists()}.
         *
         * @param defaultValue The default value to return when one has not been defined.
         * @param <T> The expected data-type of the data.
         * @return The data stored at this relative key, or default.
         */
        @SuppressWarnings("unchecked")
        public <T> T get(T defaultValue) {
            return (T) config.get(key + "." + relative, defaultValue);
        }

        /**
         * Handles nested serializers. Uses the given class as a serializer and
         * attempts to serialize an object from this relative key. Returns null
         * when the key hasn't been explicitly defined.
         *
         * @param serializerClass The non-null serializer class.
         * @param <T> The serializer type.
         * @return The serialized object.
         * @throws SerializerException If there is a mistake in config found during serialization.
         */
        public <T extends Serializer<T>> T serialize(@Nonnull Class<T> serializerClass) throws SerializerException {
            return serialize(ReflectionUtil.newInstance(serializerClass));
        }

        /**
         * Handles nested serializers. Uses the given serializer to serialize
         * an object from this relative key. Returns null when the key hasn't
         * been explicitly defined.
         *
         * @param serializer The non-null serializer instance.
         * @param <T> The serializer type.
         * @return The serialized object.
         * @throws SerializerException If there is a mistake in config found during serialization.
         */
        public <T extends Serializer<T>> T serialize(@Nonnull T serializer) throws SerializerException {

            // Use assertExists for required keys
            if (!config.contains(relative))
                return null;

            SerializeData data = new SerializeData(serializer, SerializeData.this, relative);
            return serializer.serialize(data);
        }

        public <T> T serializeNonStandardSerializer(@Nonnull Serializer<T> serializer) throws SerializerException {
            // Use assertExists for required keys
            if (!config.contains(relative))
                return null;

            SerializeData data = new SerializeData(serializer, SerializeData.this, relative);
            return serializer.serialize(data);
        }
    }
}
