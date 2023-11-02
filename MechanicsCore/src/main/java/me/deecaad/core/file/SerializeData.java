package me.deecaad.core.file;

import me.deecaad.core.mechanics.Registry;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static me.deecaad.core.file.InlineSerializer.UNIQUE_IDENTIFIER;

/**
 * {@link SerializeData} wraps a {@link ConfigurationSection} and a key along
 * with useful "validation methods". These methods will throw a
 * {@link SerializerException} if the server admin input an incorrect value.
 * This allows us, the developers, to quickly and easily check if the config is
 * valid (Without long if/else if/else chains, or otherwise). Uses a builder
 * pattern for nice one-liners.
 *
 * <p>For example, to get a positive integer from config, we can use
 * <code>SerializeData#of("your.key").assertExists().assertPositive().getInt()</code>.
 */
public class SerializeData {

    public final String serializer;
    public final File file;
    public final String key;
    public final ConfigLike config;

    /**
     * Wiki link to be used in exception messages in order to better assist
     * users in solving their issues.
     */
    public @Nullable String wikiLink;

    /**
     * The fully serialized configuration to be used in case a
     * nested-serializer uses the path-to feature. This should not be read
     * directly, instead let {@link SerializeData.ConfigAccessor#serialize(Serializer)}
     * check it automatically.
     */
    public Configuration pathToConfig;

    /**
     * If this is true, developers are using {@link #step(Serializer)}. This is
     * an advanced path-to feature which allows developers to get values from
     * config NOT STORED in the serialized object, but still under the
     * configuration section of the serializer. When this is true, we pull
     * values from 'pathToConfig' instead of 'config'
     */
    private boolean usingStep;


    public SerializeData(@NotNull String serializer, @NotNull File file, String key, @NotNull ConfigLike config) {
        this.serializer = serializer;
        this.file = file;
        this.key = key;
        this.config = config;
    }

    public SerializeData(@NotNull String serializer, @NotNull SerializeData other, @NotNull String relative) {
        this.serializer = serializer;
        this.file = other.file;
        this.key = other.getPath(relative);
        this.config = other.config;

        copyMutables(other);
    }

    public SerializeData(@NotNull Serializer<?> serializer, @NotNull File file, String key, @NotNull ConfigLike config) {
        this.serializer = getSimpleName(serializer);
        this.file = file;
        this.key = key;
        this.config = config;

        wikiLink = serializer.getWikiLink();
    }

    public SerializeData(@NotNull Serializer<?> serializer, @NotNull SerializeData other, @NotNull String relative) {
        this.serializer = getSimpleName(serializer);
        this.file = other.file;
        this.key = other.getPath(relative);
        this.config = other.config;

        copyMutables(other);
        wikiLink = serializer.getWikiLink();
    }

    @NotNull
    private SerializeData copyMutables(@NotNull SerializeData from) {
        this.wikiLink = from.wikiLink;
        this.usingStep = from.usingStep;
        return this;
    }

    @NotNull
    private static String getSimpleName(@NotNull Serializer<?> serializer) {
        return serializer.getName();
    }

    /**
     * Returns the path to the key.
     *
     * @param relative The non-null relative path.
     * @return The total path + relative path.
     */
    private String getPath(String relative) {
        return (key == null || key.isEmpty()) ? relative : (key + "." + relative);
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
    @NotNull
    public SerializeData move(@NotNull String relative) {
        return new SerializeData(serializer, this, relative).copyMutables(this);
    }

    /**
     * Shorthand for {@link #step(Serializer)}.
     *
     * @param serializer The non-null serializer that supports path-to.
     * @return The non-null serialize data.
     * @throws SerializerException If no path-to config is defined.
     * @throws InternalError       If the serializer has no default constructor.
     */
    @NotNull
    public <T extends Serializer<T>> SerializeData step(@NotNull Class<T> serializer) throws SerializerException {
        return step(ReflectionUtil.newInstance(serializer));
    }

    /**
     * Helper method to "step" into a new configuration section. Uses the
     * {@link Serializer#getKeyword()} to step into the section. Supports
     * using the {@link Serializer#canUsePathTo()} to step into other
     * files instead of just nested configuration sections.
     *
     * @param serializer The non-null serializer that supports path-to.
     * @return The non-null serialize data.
     * @throws SerializerException If no path-to config is defined.
     */
    @NotNull
    public SerializeData step(@NotNull Serializer<?> serializer) throws SerializerException {
        if (serializer.getKeyword() == null || !serializer.canUsePathTo())
            throw new IllegalArgumentException(serializer + " does not support path-to");

        // Check that the user is trying to use path-to.
        String relative = serializer.getKeyword();
        if (config instanceof BukkitConfig && config.isString(getPath(relative))) {

            // This exception should be caught by FileReader so this serializer
            // is saved for late serialization (for path-to support).
            if (pathToConfig == null)
                throw new SerializerPathToException(serializer, this);

            String path = config.getString(getPath(relative));
            SerializeData temp = new SerializeData(serializer, file, path, config); // just pass 'config' for safety's sake
            temp.copyMutables(this);
            temp.usingStep = true;
            return temp;
        }

        // Just move in when not using path-to.
        return move(relative);
    }

    @NotNull
    public ConfigListAccessor ofList() {
        String[] split = key.split("\\.");
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < split.length - 1; i++)
            key.append(split[i]).append('.');

        if (!key.isEmpty())
            key.setLength(key.length() - 1);

        return new SerializeData(serializer, file, key.toString(), config).copyMutables(this).ofList(split[split.length - 1]);
    }

    public ConfigAccessor of() {
        String[] split = key.split("\\.");
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < split.length - 1; i++)
            key.append(split[i]).append('.');

        if (!key.isEmpty())
            key.setLength(key.length() - 1);

        return new SerializeData(serializer, file, key.toString(), config).copyMutables(this).of(split[split.length - 1]);
    }

    /**
     * Creates a {@link ConfigAccessor} which accesses the data (stored in
     * config) at <code>this.key + "." + relative</code>. The returned accessor
     * can be used to validate arguments.
     *
     * @param relative The non-null, non-empty key relative to this.key.
     * @return The non-null config accessor.
     */
    @NotNull
    public ConfigAccessor of(@NotNull String relative) {
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
     * Returns <code>true</code> if the given relative config key exists.
     * Otherwise, this method will return false. Usually, you should use
     * {@link ConfigAccessor#assertExists()}.
     *
     * @param relative The non-null relative key.
     * @return <code>true</code> if the key exists.
     */
    public boolean has(String relative) {
        return usingStep ? pathToConfig.containsKey(getPath(relative)) : config.contains(getPath(relative));
    }

    /**
     * When there is no method in {@link ConfigAccessor} to match a specific
     * configuration error, you may check for it manually and use this method
     * to throw a "general" exception.
     *
     * <p>Make sure to keep messages clear and concise. There is no limit to
     * how many messages you may give to the player, but make sure that each
     * message is <i>important</i> and contains <i>useful</i> information.
     *
     * @param relative The nullable relative key.
     * @param messages The non-empty list of messages to include.
     * @return The non-null constructed exception.
     */
    @NotNull
    public SerializerException exception(@Nullable String relative, String... messages) {
        if (messages.length == 0)
            throw new IllegalArgumentException("Hey you! Yeah you! Don't be lazy, add messages!");

        String key = this.key;
        if (relative != null && !relative.isEmpty())
            key = getPath(relative);

        return new SerializerException(serializer, appendWikiLink(messages), StringUtil.foundAt(file, key));
    }

    /**
     * When there is no method in {@link ConfigListAccessor} to match a
     * specific configuration error, you may check for it manually and use this
     * method to throw a "general" exception.
     *
     * @param relative The nullable relative key.
     * @param index    The index (NOT index + 1) of the element that had the error.
     * @param messages The non-empty list of messages to include
     * @return The non-null constructed exception.
     */
    @NotNull
    public SerializerException listException(@Nullable String relative, int index, String... messages) {
        if (messages.length == 0)
            throw new IllegalArgumentException("Hey you! Yeah you! Don't be lazy, add messages!");

        String key = this.key;
        if (relative != null && !relative.isEmpty())
            key = getPath(relative);

        return new SerializerException(serializer, appendWikiLink(messages), StringUtil.foundAt(file, key, index + 1));
    }

    /**
     * Adds the wiki link, if the wiki link is not null, to the list of
     * messages. This is used for exceptions.
     *
     * @param messages The non-null array of messages to append to.
     * @return The list (with 1 more element, if the link was added).
     */
    @NotNull
    private String[] appendWikiLink(@NotNull String[] messages) {
        if (wikiLink == null || Arrays.stream(messages).anyMatch(str -> str.startsWith("Wiki: ")))
            return messages;

        String[] copy = new String[messages.length + 1];
        System.arraycopy(messages, 0, copy, 0, messages.length);
        copy[messages.length] = getWikiMessage();
        return copy;
    }

    @NotNull
    private String getWikiMessage() {
        return "Wiki: " + wikiLink;
    }


    /**
     * Wraps a configuration KEY (which points to a list of values) to some
     * helper functions to facilitate data serialization. The
     */
    public class ConfigListAccessor {

        // Stores the class arguments, which is used to check the format
        private final LinkedList<ClassArgument> arguments;
        private final String relative;
        private boolean didAssertions; // turns to true after assertList()

        public ConfigListAccessor(String relative) {
            this.arguments = new LinkedList<>();
            this.relative = relative;
        }

        @NotNull
        public ConfigListAccessor addArgument(Class<?> clazz, boolean required) {
            return this.addArgument(clazz, required, false);
        }

        @NotNull
        public ConfigListAccessor addArgument(Class<?> clazz, boolean required, boolean skipCheck) {

            // Ensure that all required arguments are in order. For example,
            // true~true~false is fine, but true~false~true is impossible to
            // serialize.
            if (required && !arguments.isEmpty() && !arguments.getLast().required)
                throw new IllegalArgumentException("Required arguments must be consecutive");

            ClassArgument arg = new ClassArgument();
            arg.clazz = clazz;
            arg.required = required;
            arg.skipCheck = skipCheck;
            arguments.add(arg);
            return this;
        }

        @NotNull
        public ConfigListAccessor assertArgumentPositive() {
            arguments.getLast().positive = true;
            return this;
        }

        @NotNull
        public ConfigListAccessor assertArgumentRange(double min, double max) {
            arguments.getLast().min = min;
            arguments.getLast().max = max;
            return this;
        }

        /**
         * Asserts that this key exists in the configuration. This method
         * ensures that the user explicitly defined a value for the key.
         *
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the key is not explicitly defined.
         */
        @NotNull
        public ConfigListAccessor assertExists() throws SerializerException {
            if (!has(relative))
                throw new SerializerMissingKeyException(serializer, relative, getLocation())
                        .addMessage(wikiLink != null, getWikiMessage());

            return this;
        }

        /**
         * If the <code>exists = true</code>, then this method will call
         * {@link #assertExists()}. This is useful for when an argument is only
         * required when another argument is present.
         *
         * @param exists true to assert if the argument exists.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the key is not explicitly defined.
         */
        @NotNull
        public ConfigListAccessor assertExists(boolean exists) throws SerializerException {
            if (exists)
                return assertExists();
            return this;
        }

        @NotNull
        @SuppressWarnings({"unchecked", "rawtypes"})
        public ConfigListAccessor assertList() throws SerializerException {
            if (arguments.isEmpty())
                throw new IllegalStateException("Need to set arguments before assertions");

            didAssertions = true;

            // The first step is to assert that the value stored at this key
            // is a list (of any generic-type).
            Object value = usingStep ? pathToConfig.getObject(getPath(relative)) : config.get(getPath(relative));
            if (value == null)
                return this;

            if (!(value instanceof List<?> list))
                throw new SerializerTypeException(serializer, List.class, value.getClass(), value, getLocation())
                        .addMessage(wikiLink != null, getWikiMessage());

            // Use assertExists for required keys
            if (list.isEmpty())
                return this;

            for (int i = 0; i < list.size(); i++) {
                String string = Objects.toString(list.get(i));

                // Show the user the correct format
                StringBuilder format = new StringBuilder("<");
                arguments.forEach(arg -> {
                    format.append(arg.clazz.getSimpleName());
                    if (arg.required) format.append('*');
                    format.append("> <");
                });
                format.append('>');

                // Empty string in config is probably a mistake (Perhaps they
                // forgot to save?). Instead of ignoring this, we should tell
                // the user (playing it safe).
                if (string == null || string.trim().isEmpty()) {
                    throw listException(relative, i, relative + " does not allow empty elements in the list.",
                            "Valid Format: " + format);
                }

                // Each element in the list should be a string of values
                // separated by a standard delimiter (Either '~' or '-' or ' ')
                String[] split = StringUtil.split(string);

                // Missing required data
                int required = (int) arguments.stream().filter(arg -> arg.required).count();
                if (split.length < required) {
                    throw listException(relative, i, relative + " requires the first " + required + " arguments to be defined.",
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
                        throw listException(relative, i, "Invalid list format, " + relative + " can only use " + arguments.size() + " arguments.",
                                SerializerException.forValue(string),
                                "Valid Format: " + format
                        );
                    }

                    String component = split[j];
                    ClassArgument argument = arguments.get(j);
                    if (argument.skipCheck)
                        continue;

                    try {
                        if (argument.clazz == int.class || argument.clazz == Integer.class) {
                            argument.clazz = Integer.class; // Set class to be more human-readable in error
                            int parseInt = Integer.parseInt(component);
                            if (!Double.isNaN(argument.min) && !Double.isNaN(argument.max) && (parseInt < argument.min || parseInt > argument.max))
                                throw new SerializerRangeException(serializer, (int) argument.min, parseInt, (int) argument.max, getLocation(i));

                            if (argument.positive && parseInt < 0)
                                throw new SerializerNegativeException(serializer, parseInt, getLocation(i));
                        } else if (argument.clazz == double.class || argument.clazz == Double.class) {
                            argument.clazz = Double.class;
                            double parseDouble = Double.parseDouble(component);
                            if (!Double.isNaN(argument.min) && !Double.isNaN(argument.max) && (parseDouble < argument.min || parseDouble > argument.max))
                                throw new SerializerRangeException(serializer, argument.min, parseDouble, argument.max, getLocation(i));

                            if (argument.positive && parseDouble < 0.0)
                                throw new SerializerNegativeException(serializer, parseDouble, getLocation(i));

                        } else if (argument.clazz == boolean.class || argument.clazz == Boolean.class) {
                            argument.clazz = Boolean.class;
                            if (!component.equalsIgnoreCase("true") && !component.equalsIgnoreCase("false"))
                                throw new Exception();

                        } else if (argument.clazz.isEnum() && EnumUtil.parseEnums((Class<Enum>) argument.clazz, component).isEmpty()) {
                            throw new SerializerEnumException(serializer, (Class<Enum>) argument.clazz, component, true, getLocation(i));
                        }
                    } catch (SerializerException ex) {
                        throw ex.addMessage("Full List Element: " + string)
                                .addMessage("Valid List Format: " + format)
                                .addMessage(wikiLink != null, getWikiMessage()); // Rethrow exception so it isn't caught and ignored
                    } catch (Exception ex) {
                        throw new SerializerTypeException(serializer, argument.clazz, null, component, getLocation(i))
                                .addMessage("Full List Element: " + string)
                                .addMessage("Valid List Format: " + format)
                                .addMessage(wikiLink != null, getWikiMessage());
                    }
                }
            }

            return this;
        }

        public List<String[]> get() {
            if (!didAssertions)
                throw new IllegalStateException("Forgot to call assertList()? Did something go wrong?");

            // Use assertExists for required keys
            if (!has(relative))
                return Collections.emptyList();

            List<String[]> list = new ArrayList<>();
            List<?> configList = usingStep ? pathToConfig.getList(getPath(relative)) : config.getList(getPath(relative));
            for (Object obj : configList) {
                list.add(StringUtil.split(obj.toString()));
            }

            return list;
        }

        public Stream<String[]> stream() {
            return get().stream();
        }

        public String getLocation() {
            String stepAddon = usingStep ? " (File location will be inaccurate since you are using path-to)" : "";
            if (relative == null || relative.isEmpty()) {
                return config.getLocation(file, key) + stepAddon;
            } else {
                return config.getLocation(file, getPath(relative)) + stepAddon;
            }
        }

        public String getLocation(int index) {
            String stepAddon = usingStep ? " (File location will be inaccurate since you are using path-to)" : "";
            if (relative == null || relative.isEmpty()) {
                return StringUtil.foundAt(file, key, index + 1) + stepAddon;
            } else {
                return StringUtil.foundAt(file, getPath(relative), index + 1) + stepAddon;
            }
        }

        private static class ClassArgument {
            Class<?> clazz;
            boolean required;
            boolean skipCheck;

            boolean positive;
            double min = Double.NaN;
            double max = Double.NaN;
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
        private boolean exists;

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
        @NotNull
        public ConfigAccessor assertExists() throws SerializerException {
            if (!has(relative))
                throw new SerializerMissingKeyException(serializer, relative, getLocation())
                        .addMessage(wikiLink != null, getWikiMessage());

            exists = true;
            return this;
        }

        /**
         * If the <code>exists = true</code>, then this method will call
         * {@link #assertExists()}. This is useful for when an argument is only
         * required when another argument is present.
         *
         * @param exists true to assert if the argument exists.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the key is not explicitly defined.
         */
        @NotNull
        public ConfigAccessor assertExists(boolean exists) throws SerializerException {
            if (exists)
                return assertExists();
            else
                return this;
        }

        /**
         * Returns <code>true</code> when the object stored in this location
         * matches the given <code>type</code>.
         *
         * @param type Which type to check for
         * @return true, if the value matched the type.
         */
        public boolean is(@NotNull Class<?> type) {
            if (type == int.class || type == short.class || type == long.class || type == float.class || type == double.class || type == boolean.class || type == char.class || type == byte.class) {
                throw new IllegalArgumentException("Silly developer, these are primitive types! Check wrapper classes instead.");
            }

            Object value = usingStep ? pathToConfig.getObject(getPath(relative)) : config.get(getPath(relative));
            return value != null && type.isAssignableFrom(value.getClass());
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
        @NotNull
        public ConfigAccessor assertType(@NotNull Class<?> type) throws SerializerException {
            Object value = usingStep ? pathToConfig.getObject(getPath(relative)) : config.get(getPath(relative));

            // Use assertExists for required keys
            if (value != null) {
                Class<?> actual = value.getClass();
                if (!type.isAssignableFrom(actual)) {
                    throw new SerializerTypeException(serializer, type, actual, value, getLocation())
                            .addMessage(wikiLink != null, getWikiMessage());
                }
            }

            return this;
        }

        /**
         * Returns the integer value of the config, or throws an exception if
         * the value is not a number. Note that this method will also throw an
         * exception if the input is explicitly a double. For example,
         * <code>1.0</code> is a valid integer which will be parsed as
         * <code>1</code>, but <code>1.1</code> will throw an exception.
         *
         * <p>Note that this method should only be called if you have already
         * used {@link #assertExists()}. For non-required values, instead use
         * {@link #getInt(int)}.
         *
         * @return The integer from config.
         * @throws SerializerException If the config value is not an integer.
         */
        public int getInt() throws SerializerException {
            if (!exists)
                throw new IllegalStateException("Either provide a default value or use assertExists()!");

            return getInt(Integer.MIN_VALUE);
        }

        /**
         * Returns the integer value of the config, or throws an exception if
         * the value is not a number. Note that this method will also throw an
         * exception if the input is explicitly a double. For example,
         * <code>1.0</code> is a valid integer which will be parsed as
         * <code>1</code>, but <code>1.1</code> will throw an exception.
         *
         * @param def The default value to return when the config is undefined.
         * @return The integer from config.
         * @throws SerializerException If the config value is not an integer.
         */
        public int getInt(int def) throws SerializerException {
            Number num = Objects.requireNonNull(getNumber(def));
            if (Double.compare(Math.floor(num.doubleValue()), Math.ceil(num.doubleValue())) != 0)
                throw new SerializerTypeException(serializer, Integer.class, Double.class, num, getLocation())
                        .addMessage(wikiLink != null, getWikiMessage());

            return num.intValue();
        }

        /**
         * Returns the double value of the config, or throws an exception if
         * the value is not a number.
         *
         * <p>Note that this method should only be called if you have already
         * used {@link #assertExists()}. For non-required values, instead use
         * {@link #getDouble(double)}.
         *
         * @return The integer from config.
         * @throws SerializerException If the config value is not a double.
         */
        public double getDouble() throws SerializerException {
            if (!exists)
                throw new IllegalArgumentException("Either provide a default value or use assertExists()!");

            return getDouble(Double.NaN);
        }

        /**
         * Returns the double value of the config, or throws an exception if
         * the value is not a number.
         *
         * @param def The default value to return when the config is undefined.
         * @return The double from config.
         * @throws SerializerException If the config value is not a double.
         */
        public double getDouble(double def) throws SerializerException {
            return Objects.requireNonNull(getNumber(def)).doubleValue();
        }

        /**
         * Returns the boolean value of the config, or throws an exception if
         * the value is not a boolean.
         *
         * <p>Note that this method should only be called if you have already
         * used {@link #assertExists()}. For non-required values, instead use
         * {@link #getBool(boolean)}.
         *
         * @return The boolean from config.
         * @throws SerializerException If the config value is not a boolean.
         */
        public boolean getBool() throws SerializerException {
            if (!exists)
                throw new IllegalStateException("Either provide a default value or use assertExists()!");

            return getBool(false);
        }

        /**
         * Returns the boolean value of the config, or throws an exception if
         * the value is not a boolean.
         *
         * @param def The default value to return when the config is undefined.
         * @return The boolean from config.
         * @throws SerializerException If the config value is not a boolean.
         */
        public boolean getBool(boolean def) throws SerializerException {
            Object value = usingStep ? pathToConfig.getObject(getPath(relative)) : config.get(getPath(relative));
            if (value == null)
                return def;

            if (value instanceof Boolean)
                return (Boolean) value;
            if (value instanceof String) {
                if (value.toString().trim().equalsIgnoreCase("true"))
                    return true;
                if (value.toString().trim().equalsIgnoreCase("false"))
                    return false;
            }

            throw new SerializerTypeException(serializer, Boolean.class, value.getClass(), value, getLocation())
                    .addMessage(wikiLink != null, getWikiMessage());
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
        @Nullable
        public Number getNumber(@Nullable Number def) throws SerializerException {
            Object value = usingStep ? pathToConfig.getObject(getPath(relative)) : config.get(getPath(relative));

            // Use assertExists for required keys
            if (value == null)
                return def;

            // If the value is a string, attempt to parse it as a number
            if (value instanceof String str) {
                try {
                    value = Double.valueOf(str);
                } catch (NumberFormatException ex) {
                    throw new SerializerTypeException(serializer, Number.class, value.getClass(), value, getLocation())
                            .addMessage(wikiLink != null, getWikiMessage());
                }
            }

            try {
                return (Number) value;
            } catch (ClassCastException ex) {
                throw new SerializerTypeException(serializer, Number.class, value.getClass(), value, getLocation())
                        .addMessage(wikiLink != null, getWikiMessage());
            }
        }

        /**
         * Asserts that the value at this key is a number, AND that the number
         * is positive. Note that if you want a more specific number type (for
         * example, an integer), you should use {@link #getInt(int)}.
         *
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the type is not a number or is not positive.
         */
        @NotNull
        public ConfigAccessor assertPositive() throws SerializerException {
            Number value = getNumber(null);

            // Use assertExists for required keys
            if (value != null) {

                if ((value instanceof Long && value.longValue() < 0L) || value.doubleValue() < 0L)
                    throw new SerializerNegativeException(serializer, value, getLocation())
                            .addMessage(wikiLink != null, getWikiMessage());
            }

            return this;
        }

        /**
         * Asserts that the value at this key is a number, AND that the number
         * is within the inclusive range. Note that if you want a more specific
         * number type (for example, an integer), you should use
         * {@link #getInt(int)}.
         *
         * @param min Inclusive minimum bound.
         * @param max Inclusive maximum bound.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException      If the value is not in range.
         * @throws IllegalArgumentException If min larger than max.
         */
        @NotNull
        public ConfigAccessor assertRange(int min, int max) throws SerializerException {
            if (min > max)
                throw new IllegalArgumentException("min > max");

            Number value = getNumber(null);

            // Use assertExists for required keys
            if (value != null) {

                // Silently strips away float point data (without exception)
                int num = value.intValue();
                if (num < min || num > max)
                    throw new SerializerRangeException(serializer, min, num, max, getLocation())
                            .addMessage(wikiLink != null, getWikiMessage());
            }

            return this;
        }

        /**
         * Asserts that the value at this key is a number, AND that the number
         * is within the inclusive range. Note that if you want a more specific
         * number type (for example, an integer), you should use
         * {@link #getInt(int)}.
         *
         * @param min Inclusive minimum bound.
         * @param max Inclusive maximum bound.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException      If the value is not in range.
         * @throws IllegalArgumentException If min larger than max.
         */
        @NotNull
        public ConfigAccessor assertRange(double min, double max) throws SerializerException {
            if (min > max)
                throw new IllegalArgumentException("min > max");

            Number value = getNumber(null);

            // Use assertExists for required keys
            if (value != null) {

                double num = value.doubleValue();
                if (num < min || num > max)
                    throw new SerializerRangeException(serializer, min, num, max, getLocation())
                            .addMessage(wikiLink != null, getWikiMessage());
            }

            return this;
        }

        @NotNull
        public String getLocation() {
            String stepAddon = usingStep ? " (File location will be inaccurate since you are using path-to)" : "";
            if (relative == null || relative.isEmpty()) {
                return config.getLocation(file, key) + stepAddon;
            } else {
                return config.getLocation(file, getPath(relative)) + stepAddon;
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
        @NotNull
        public <T> T get() {
            if (!exists)
                throw new IllegalStateException("Either provide a default value or use assertExists()!");

            return (T) (usingStep ? pathToConfig.getObject(getPath(relative)) : config.get(getPath(relative)));
        }

        /**
         * Gets the data stored at this relative key, or
         * <code>defaultValue</code> if the key is not explicitly defined. It
         * does not make sense to use this method when there has been a
         * previous call to {@link #assertExists()}.
         *
         * @param defaultValue The default value to return when one has not been defined.
         * @param <T>          The expected data-type of the data.
         * @return The data stored at this relative key, or default.
         */
        @Nullable
        public <T> T get(T defaultValue) {
            // noinspection unchecked
            return (T) (usingStep ? pathToConfig.getObject(getPath(relative), defaultValue) : config.get(getPath(relative), defaultValue));
        }

        /**
         * Shorthand for using {@link #getEnum(Class, Enum)} and returning
         * <code>null</code> by default.
         *
         * @param clazz The non-null enum class that is expected.
         * @param <T>   The enum type.
         * @return The user input enum value, or null.
         * @throws SerializerException If the user defined an invalid type.
         */
        @Nullable
        public <T extends Enum<T>> T getEnum(@NotNull Class<T> clazz) throws SerializerException {
            if (!exists)
                throw new IllegalStateException("Either provide a default value or use assertExists()!");

            return getEnum(clazz, null);
        }

        /**
         * Serializes an enum value from config. If the key is not defined,
         * then <code>defaultValue</code> is returned. If the user defines a
         * string that doesn't match any enum, a {@link SerializerEnumException}
         * is thrown.
         *
         * @param clazz        The non-null enum class.
         * @param defaultValue The default value to use when a key is undefined.
         * @param <T>          The enum type.
         * @return The serialized enum type, or defaultValue.
         * @throws SerializerException If there is a misconfiguration in config.
         */
        @Nullable
        public <T extends Enum<T>> T getEnum(@NotNull Class<T> clazz, @Nullable T defaultValue) throws SerializerException {
            String input = usingStep ? pathToConfig.getString(getPath(relative)) : config.getString(getPath(relative));

            // Use assertExists for required keys
            if (input == null || input.isBlank())
                return defaultValue;

            // Wildcards are not allowed for singleton enums, they are only
            // allowed for lists.
            input = input.trim();
            if (input.startsWith("$"))
                throw new SerializerEnumException(serializer, clazz, input, false, getLocation())
                        .addMessage(wikiLink != null, getWikiMessage());

            // The returned value will have either 0 elements (meaning that the
            // input is invalid) OR 1 element (meaning that the input is valid).
            List<T> list = EnumUtil.parseEnums(clazz, input);
            if (list.isEmpty()) {
                throw new SerializerEnumException(serializer, clazz, input, false, getLocation())
                        .addMessage(wikiLink != null, getWikiMessage());
            }

            // At this point, the list is guaranteed to have exactly 1 element.
            return list.get(0);
        }

        @Nullable
        public <T extends Keyed> T getKeyed(@NotNull org.bukkit.Registry<T> registry) throws SerializerException {
            if (!exists)
                throw new IllegalStateException("Either provide a default value or use assertExists()!");

            return getKeyed(registry, null);
        }

        @Nullable
        public <T extends Keyed> T getKeyed(@NotNull org.bukkit.Registry<T> registry, @Nullable T defaultValue) throws SerializerException {
            Object value = usingStep ? pathToConfig.getObject(getPath(relative), "") : config.get(getPath(relative), "");
            String input = value.toString().trim().toLowerCase(Locale.ROOT);

            // Use assertExists for required keys
            if (input.isEmpty())
                return defaultValue;

            // Keys use namespace:key, where namespace is usually minecraft.
            // We don't want to force people to use "minecraft:", but this
            // code assumes that there will be custom namespaces. So the
            // "minecraft:" namespace can be omitted since it is the default.
            NamespacedKey key = null;
            Set<String> options = new LinkedHashSet<>();
            String registryName = "Registry";
            for (T element : registry) {
                key = element.getKey();
                registryName = element.getClass().getSimpleName();

                // 'options' is for when the user puts in a bad value, so we
                // can give them options to choose from to replace the bad value
                options.add(key.toString());
                if (NamespacedKey.MINECRAFT.equals(key.getNamespace()))
                    options.add(key.getKey());

                if (input.equals(key.toString()))
                    break;
                if (NamespacedKey.MINECRAFT.equals(key.getNamespace()) && input.equals(key.getKey()))
                    break;

                // Reset to null since it was not a match
                key = null;
            }

            // Make sure we have a match
            if (key == null)
                throw new SerializerOptionsException(serializer, registryName, options, value.toString(), getLocation());

            T returnValue = registry.get(key);
            if (returnValue == null)
                throw new RuntimeException("This should never occur");

            return returnValue;
        }

        /**
         * Returns the string value of the config, adjusted to fit the
         * adventure format. Adventure text is formatting using html-like tags
         * instead of the legacy <code>{@literal &}</code> symbol. If the string in config
         * contains the legacy color system, we will attempt to convert it.
         *
         * <p>The returned string should be parsed using
         * {@link net.kyori.adventure.text.minimessage.MiniMessage}. You may
         * use MechanicsCore's instance {@link me.deecaad.core.MechanicsCore#message}.
         *
         * <p>Note that this method should only be called if you have already
         * used {@link #assertExists()}. For non-required values, instead use
         * {@link #getAdventure(String)}.
         *
         * @return The converted string from config.
         */
        @Nullable
        public String getAdventure() {
            if (!exists)
                throw new IllegalStateException("Either provide a default value or use assertExists()!");

            return getAdventure(null);
        }

        /**
         * Returns the string value of the config, adjusted to fit the
         * adventure format. Adventure text is formatting using html-like tags
         * instead of the legacy <code>{@literal &}</code> symbol. If the string in config
         * contains the legacy color system, we will attempt to convert it.
         *
         * <p>The returned string should be parsed using
         * {@link net.kyori.adventure.text.minimessage.MiniMessage}. You may
         * use MechanicsCore's instance {@link me.deecaad.core.MechanicsCore#message}.
         *
         * @return The converted string from config.
         */
        @Nullable
        public String getAdventure(@Nullable String defaultValue) {
            if (!has(relative))
                return defaultValue;

            String value = usingStep ? pathToConfig.getString(getPath(relative)) : config.getString(getPath(relative));
            assert value != null;

            return StringUtil.colorAdventure(value);
        }

        /**
         * Returns one type from a registry. The exact type is unknown, and is
         * determined by the {@link InlineSerializer#UNIQUE_IDENTIFIER} present
         * in configuration. Requires a previous call to {@link #assertExists()}.
         *
         * @param registry The non-null registry of possible types to use.
         * @param <T>      The superclass type.
         * @return A serialized instance.
         * @throws SerializerException If there are any errors in config.
         */
        @Nullable
        public <T extends InlineSerializer<T>> T getRegistry(Registry<T> registry) throws SerializerException {
            if (!exists)
                throw new IllegalStateException("Either provide a default value or use assertExists()!");

            return getRegistry(registry, null);
        }

        /**
         * Returns one type from a registry. The exact type is unknown, and is
         * determined by the {@link InlineSerializer#UNIQUE_IDENTIFIER} present
         * in configuration. If no value has been defined in config, then the
         * default value is returned.
         *
         * @param registry     The non-null registry of possible types to use.
         * @param defaultValue What to return if no value exists in config.
         * @param <T>          The superclass type.
         * @return A serialized instance.
         * @throws SerializerException If there are any errors in config.
         */
        @Nullable
        public <T extends InlineSerializer<T>> T getRegistry(@NotNull Registry<T> registry, @Nullable T defaultValue) throws SerializerException {
            if (!(config instanceof MapConfigLike mapLike))
                throw new UnsupportedOperationException("Cannot use registries with " + config);
            if (!has(relative))
                return defaultValue;

            Map<String, MapConfigLike.Holder> map = assertType(Map.class).assertExists().get();
            ConfigLike temp = new MapConfigLike(map).setDebugInfo(mapLike.getFile(), mapLike.getPath(), mapLike.getFullLine());
            SerializeData nested = new SerializeData(serializer, file, null, temp);

            String key = nested.of(UNIQUE_IDENTIFIER).assertExists().assertType(String.class).get();
            T base = registry.get(key);

            if (base == null)
                throw new SerializerOptionsException(serializer, registry.getKey(), registry.getOptions(), key, getLocation());

            return base.serialize(nested);
        }

        /**
         * This method is similar to {@link #getRegistry(Registry)}, but
         * instead of allowing every type from a registry, 1 specific type is
         * allowed.
         *
         * @param impliedType      The serializer.
         * @param <SerializerType> Which type of the serializer.
         * @param <SerializedType> The type to create.
         * @return The serialized instance.
         * @throws SerializerException If there are any errors in config.
         */
        public <SerializerType extends Serializer<SerializedType>, SerializedType> @Nullable SerializedType getImplied(
                @NotNull SerializerType impliedType
        ) throws SerializerException {
            if (!(config instanceof MapConfigLike mapLike))
                throw new UnsupportedOperationException("Cannot use registries with " + config);
            if (!has(relative))
                return null;

            Map<String, ?> map = (Map<String, ?>) config.get(getPath(relative));

            // We have to make sure that the user used the "JSON Format" in the string.
            if (map.containsKey(UNIQUE_IDENTIFIER) && !Registry.matches(map.get(UNIQUE_IDENTIFIER).toString(), impliedType.getKeyword()))
                throw exception(relative, "Expected a '" + impliedType.getKeyword() + "' but got a '" + map.get(UNIQUE_IDENTIFIER) + "'");

            ConfigLike temp = new MapConfigLike((Map<String, MapConfigLike.Holder>) map).setDebugInfo(mapLike.getFile(), mapLike.getPath(), mapLike.getFullLine());
            SerializeData nested = new SerializeData(impliedType, file, null, temp);

            return impliedType.serialize(nested);
        }

        @NotNull
        public <T extends InlineSerializer<T>> List<T> getRegistryList(@NotNull Registry<T> registry) throws SerializerException {
            if (!(config instanceof MapConfigLike mapLike))
                throw new UnsupportedOperationException("Cannot use registries with " + config);
            if (!has(relative))
                return List.of();

            List<MapConfigLike.Holder> list = (List<MapConfigLike.Holder>) config.getList(getPath(relative));
            List<T> returnValue = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                MapConfigLike.Holder holder = list.get(i);

                // We have to make sure that the user used the "JSON Format" in the string.
                if (!(holder.value() instanceof Map<?, ?> map))
                    throw listException(relative, i, "Expected an inline serializer like 'sound(sound=ENTITY_GENERIC_EXPLOSION)', but instead got '" + holder.value() + "'");
                if (!map.containsKey(UNIQUE_IDENTIFIER))
                    throw listException(relative, i, "Missing name for a(n) '" + serializer + "'");

                String id = ((MapConfigLike.Holder) map.get(UNIQUE_IDENTIFIER)).value().toString();
                InlineSerializer<T> serializer = registry.get(id);
                if (serializer == null)
                    throw new SerializerOptionsException(SerializeData.this.serializer, registry.getKey(), registry.getOptions(), id, getLocation());

                ConfigLike temp = new MapConfigLike((Map<String, MapConfigLike.Holder>) map).setDebugInfo(mapLike.getFile(), mapLike.getPath(), mapLike.getFullLine());
                SerializeData nested = new SerializeData(serializer, file, null, temp);

                returnValue.add(serializer.serialize(nested));
            }

            return returnValue;
        }

        @NotNull
        public <T extends InlineSerializer<T>> List<T> getImpliedList(T impliedType) throws SerializerException {
            if (!(config instanceof MapConfigLike mapLike))
                throw new UnsupportedOperationException("Cannot use registries with " + config);
            if (!has(relative))
                return List.of();

            List<MapConfigLike.Holder> list = (List<MapConfigLike.Holder>) config.getList(getPath(relative));
            List<T> returnValue = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                MapConfigLike.Holder holder = list.get(i);

                // We have to make sure that the user used the "JSON Format" in the string.
                if (!(holder.value() instanceof Map<?, ?> map))
                    throw listException(relative, i, "Expected an inline serializer like 'sound(sound=ENTITY_GENERIC_EXPLOSION)', but instead got '" + holder.value() + "'");
                if (map.containsKey(UNIQUE_IDENTIFIER) && !Registry.matches(map.get(UNIQUE_IDENTIFIER).toString(), impliedType.getKeyword()))
                    throw listException(relative, i, "Expected a '" + impliedType.getInlineKeyword() + "' but got a '" + map.get(UNIQUE_IDENTIFIER) + "'");

                ConfigLike temp = new MapConfigLike((Map<String, MapConfigLike.Holder>) map).setDebugInfo(mapLike.getFile(), mapLike.getPath(), mapLike.getFullLine());
                SerializeData nested = new SerializeData(impliedType, file, null, temp);

                returnValue.add(impliedType.serialize(nested));
            }

            return returnValue;
        }

        /**
         * Handles nested serializers. Uses the given class as a serializer and
         * attempts to serialize an object from this relative key. Returns null
         * when the key hasn't been explicitly defined.
         *
         * @param serializerClass The non-null serializer class.
         * @param <T>             The serializer type.
         * @return The serialized object.
         * @throws SerializerException If there is a mistake in config found during serialization.
         */
        @Nullable
        public <T extends Serializer<T>> T serialize(@NotNull Class<T> serializerClass) throws SerializerException {
            return serialize(ReflectionUtil.newInstance(serializerClass));
        }

        /**
         * Handles nested serializers. Uses the given serializer to serialize
         * an object from this relative key. Returns null when the key hasn't
         * been explicitly defined.
         *
         * @param serializer The non-null serializer instance.
         * @param <T>        The serializer type.
         * @return The serialized object.
         * @throws SerializerException If there is a mistake in config found during serialization.
         */
        @Nullable
        public <T> T serialize(@NotNull Serializer<T> serializer) throws SerializerException {

            // Use assertExists for required keys
            if (!has(relative))
                return null;

            SerializeData data = new SerializeData(serializer, SerializeData.this, relative);
            data.copyMutables(SerializeData.this);

            // Allow path-to compatibility when using nested serializers
            boolean isString = usingStep ? pathToConfig.getString(getPath(relative)) == null : config.isString(getPath(relative));
            if (serializer.canUsePathTo() && isString) {

                if (usingStep)
                    throw exception(relative, "Tried to use doubly nested path-to. This is is not a supported option.");

                String path = config.getString(getPath(relative));

                // In order for path-to to work, the serializer needs to have a
                // keyword so the FileReader automatically serializes it.
                if (serializer.getKeyword() == null)
                    throw new SerializerPathToException(serializer, data);

                // If we don't have access to the serialized config, we cannot
                // attempt a path-to.
                if (pathToConfig == null)
                    throw new SerializerPathToException(serializer, data);

                // Check to make sure the path points to a serialized object
                Object obj = pathToConfig.getObject(path);
                if (obj == null)
                    throw exception(relative, "Found an invalid path when using 'Path To' feature",
                            "Path '" + path + "' could not be found. Check for errors above this message.");

                // Technically not "perfect" since a serializer can return a
                // non-serializer object. ItemSerializer is covered with its
                // own item-registry system, and other cases are unlikely to
                // happen since the config is too small for them.
                if (!serializer.getClass().isInstance(obj))
                    throw exception(relative, "Found an invalid object when using 'Path To' feature",
                            "Path '" + path + "' pointed to an improper object type.",
                            "Should have been '" + serializer.getClass().getSimpleName() + "', but instead got '" + obj.getClass().getSimpleName() + "'",
                            SerializerException.forValue(obj));

                // Generic fuckery
                return (T) serializer.getClass().cast(obj);
            }

            return serializer.serialize(data);
        }
    }
}
