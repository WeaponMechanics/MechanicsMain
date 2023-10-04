package me.deecaad.core.file;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * This interface outlines the methods needed to pull values from a
 * configuration file. Implementors should have compatibility with bukkit's
 * {@link ConfigurationSection}. Using an implementation of this config instead
 * of bukkit's config should have the advantage of better performance.
 *
 * <p>Configuration files are YAML files, which are files that follow a
 * key-value pair format. The key is a unique {@link String}, but the value can
 * be any {@link Object}.
 */
public interface Configuration {

    /**
     * Adds every key-value pair from the bukkit configuration into this
     * configuration. If a key that exists in this configuration also exists in
     * the bukkit configuration, it is saved separately then thrown as a
     * {@link DuplicateKeyException}.
     *
     * @param config The bukkit configuration to copy key-value pairs from.
     * @throws DuplicateKeyException Thrown if a key is attempted to be added
     *                               when it already exists in this config.
     */
    void add(ConfigurationSection config) throws DuplicateKeyException;

    /**
     * Adds every key-value pair from the configuration into this
     * configuration. If a key that exists in this configuration also exists in
     * the given configuration, it is saved separately then thrown as a
     * {@link DuplicateKeyException}.
     *
     * @param config The configuration to copy key-value pairs from.
     * @throws DuplicateKeyException Thrown if a key is attempted to be added
     *                               when it already exists in this config.
     */
    void add(Configuration config) throws DuplicateKeyException;

    /**
     * Sets the key-value pair for the given <code>key</code>. If the key
     * already exists, the value previously assigned to it is returned,
     * and the new <code>value</code> is assigned.
     *
     * @param key   The key location to assign the <code>value</code> to.
     * @param value The value to assign to the <code>location</code>.
     * @return The value that was previously assigned to the <code>key</code>,
     *         or null.
     */
    @Nullable
    Object set(String key, Object value);

    /**
     * Returns an immutable set of the every key present. This is most useful
     * for looping through every config key. Use {@link #containsKey(String)}
     * if you want to check for a key's existence.
     *
     * @return Immutable, non-null set of all keys.
     */
    @NotNull
    Set<String> getKeys();

    /**
     * Returns the integer present at the given <code>key</code>. If there is a
     * number at that location that is not an integer, that number is casted to
     * an integer. If the value at that <code>key</code> does not exist, or it
     * is not a number, <code>0</code> is returned.
     *
     * @param key The location to look for a value.
     * @return The integer found.
     */
    int getInt(@NotNull String key);

    /**
     * Returns the integer present at the given <code>key</code>. If there is a
     * number at that location that is not an integer, that number is casted to
     * an integer. If the value at that <code>key</code> does not exist, or it
     * is not a number, <code>def</code> is returned.
     *
     * @param key The location to look for a value.
     * @param def The default value to use if the <code>key</code> does not
     *            exist or the value is not a number.
     * @return The integer found.
     */
    int getInt(@NotNull String key, int def);

    /**
     * Returns the double present at the given <code>key</code>. If there is a
     * number at that location that is not a double, that number is casted to a
     * double. If the value at that <code>key</code> does not exist, or it is
     * not a number, <code>0.0</code> is returned.
     *
     * @param key The location to look for a value.
     * @return The double found.
     */
    double getDouble(@NotNull String key);

    /**
     * Returns the double present at the given <code>key</code>. If there is a
     * number at that location that is not a double, that number is casted to a
     * double. If the value at that <code>key</code> does not exist, or it is
     * not a number, <code>def</code> is returned.
     *
     * @param key The location to look for a value.
     * @param def The default value to use if the <code>key</code> does not
     *            exist or the value is not a number.
     * @return The double found.
     */
    double getDouble(@NotNull String key, double def);

    /**
     * Returns the {@link String} present at the given <code>key</code>. If the
     * value at that location is not a {@link String}, or if the key is not
     * present, then this method should return <code>null</code>.
     *
     * @param key The location to look for a value.
     * @return The {@link String} found.
     */
    @Nullable String getString(@NotNull String key);

    /**
     * Returns the {@link String} present at the given <code>key</code>. If the
     * value at that location is not a {@link String}, or if the key is not
     * present, then this method should return <code>def</code>.
     *
     * @param key The location to look for a value.
     * @param def The default value to use if the <code>key</code> does not
     *            exist or the value is not a {@link String}.
     * @return The {@link String} found.
     */
    @Contract("_, null -> null; _, !null -> !null")
    @Nullable
    String getString(String key, @Nullable String def);

    /**
     * Returns the boolean present at the given <code>key</code>. If the value
     * at that location is not a boolean, or if the key is not present, then
     * this method should return <code>false</code>.
     *
     * @param key The location to look for a value.
     * @return The boolean found.
     */
    boolean getBool(@NotNull String key);

    /**
     * Returns the boolean present at the given <code>key</code>. If the value
     * at that location is not a boolean, or if the key is not present, then
     * this method should return <code>false</code>.
     *
     * @param key The location to look for a value.
     * @param def The default value to use if the <code>key</code> does not
     *            exist or the value is not a boolean.
     * @return The boolean found.
     */
    boolean getBool(@NotNull String key, boolean def);

    /**
     * Returns the {@link List} of {@link String}s at the given
     * <code>key</code>. If the value at that location is not a {@link List}
     * of {@link String}s, or if the key is not present, then this method
     * should return an immutable empty list.
     *
     * <p>If you want to modify this list, you should create a deep copy of the
     * returned {@link List}, modify that, and set it using
     * {@link #set(String, Object)}.
     *
     * @see Collections#emptyList()
     *
     * @param key The location to pull the value from.
     * @return The pulled value.
     */
    @NotNull
    List<String> getList(@NotNull String key);

    /**
     * Returns the {@link List} of {@link String}s at the given
     * <code>key</code>. If the value at that location is not a {@link List}
     * of {@link String}s, or if the key is not present, then this method
     * should return <code>def</code>.
     *
     * @param key The location to pull the value from.
     * @param def The default value to use if the <code>key</code> does not
     *            exist or the value is not a {@link List} of {@link String}s.
     * @return The pulled value.
     */
    @Contract("_, null -> null; _, !null -> !null")
    @Nullable
    List<String> getList(@NotNull String key, @Nullable List<String> def);

    /**
     * Returns the {@link Object} at the given <code>key</code>. If the key is
     * not present, then this method should return <code>null</code>.
     *
     * @param key The location to pull the value from.
     * @return The pulled value.
     */
    @Nullable
    Object getObject(@NotNull String key);

    /**
     * Returns the {@link Object} at the given <code>key</code>. If the key is
     * not present, then this method should return <code>def</code>.
     *
     * @param key The location to pull the value from.
     * @param def The default value to use if the <code>key</code> does not
     *            exist.
     * @return The pulled value.
     */
    @Contract("_, null -> null; _, !null -> !null")
    @Nullable
    Object getObject(@NotNull String key, @Nullable Object def);

    /**
     * Returns the {@link Object} at the given <code>key</code>. The value is
     * casted to the generic type of <code>clazz</code>. If the key is not
     * present, then this method should return <code>null</code>.
     *
     * @param key The location to pull the value from.
     * @param clazz The class that defines the class type.
     * @param <T> The class type to cast the value to.
     * @return The pulled value.
     */
    @Nullable
    <T> T getObject(@NotNull String key, @NotNull Class<T> clazz);

    /**
     * Returns the {@link Object} at the given <code>key</code>. The value is
     * casted to the generic type of <code>clazz</code>. If the key is not
     * present, then this method should return <code>null</code>.
     *
     * @param key The location to pull the value from.
     * @param def The default value to use if the <code>key</code> does not
     *            exist.
     * @param clazz The class that defines the class type.
     * @param <T> The class type to cast the value to.
     * @return The pulled value.
     */
    @Contract("_, null, _ -> null; _, !null, _ -> !null")
    @Nullable
    <T> T getObject(@NotNull String key, @Nullable T def, @NotNull Class<T> clazz);

    /**
     * Returns <code>true</code> if the given <code>key</code> is present in
     * this configuration. Otherwise, this method will return
     * <code>false</code>.
     *
     * @param key The key to check for.
     * @return <code>true</code> if the <code>key</code> is present.
     */
    boolean containsKey(@NotNull String key);

    /**
     * Returns <code>true</code> if the given <code>key</code> is present in
     * this configuration and the value is an instance of the given
     * {@link Class} <code>clazz</code>.
     *
     * <p>Since generics cannot store primitive data types, it is important
     * that you use the wrapper classes when checking for numbers/booleans.
     * For example, use <code>Boolean.class</code> instead of
     * <code>boolean.class</code>.
     *
     * @param key The key to check for.
     * @param clazz The class type to check for.
     * @return <code>true</code> if the <code>key</code> is present and if the
     *         value matches the class type.
     */
    boolean containsKey(@NotNull String key, @NotNull Class<?> clazz);

    /**
     * Removes every key-value pair from the backing data structures, leaving
     * them for garbage collection later.
     */
    void clear();

    /**
     * Loops through every key that starts with
     * <code>path</code> and checks to see if the
     * key is "deep" or not
     *
     * @param path     The starting path
     * @param consumer What to do with every key
     * @param deep     true if should go deep
     */
    void forEach(@NotNull String path, @NotNull BiConsumer<String, Object> consumer, boolean deep);
}
