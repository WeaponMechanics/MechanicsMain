package me.deecaad.core.file;

import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public interface Configuration {

    /**
     * Adds every key and value from the given <code>ConfigurationSection</code>
     * to this configuration. If a key is added that already exists in
     * this <code>Configuration</code>, then a <code>DuplicateKeyException</code>
     * is thrown.
     *
     * @param config The configuration to pull from
     * @throws DuplicateKeyException If a duplicate key is found
     */
    void add(ConfigurationSection config) throws DuplicateKeyException;

    /**
     * Adds every key and value from the given <code>Configuration</code>
     * to this configuration. If a key is added that already exists in
     * this <code>Configuration</code>, then a <code>DuplicateKeyException</code>
     * is thrown.
     *
     * @param config The configuration to pull from
     * @throws DuplicateKeyException If a duplicate key is found
     */
    void add(Configuration config) throws DuplicateKeyException;

    /**
     * Sets the value at the given key
     *
     * @param key Location to set
     * @param value The value to set at the location
     * @return The value previously at that location, or null
     */
    @Nullable
    Object set(String key, Object value);

    /**
     * Gets every key present in this <code>Configuration</code>
     *
     * @return All keys
     */
    Set<String> getKeys();

    /**
     * Get the <code>int</code> value stored at
     * the given key
     *
     * @param key The location to pull the value from
     * @return The pulled value
     */
    int getInt(String key);

    /**
     * Get the <code>int</code> value stored at
     * the given key. If the key is not present,
     * the default value is returned.
     *
     * @param key The location to pull the value from
     * @param def The default value
     * @return The pulled value
     */
    int getInt(String key, int def);

    /**
     * Get the <code>double</code> value stored at
     * the given key
     *
     * @param key The location to pull the value from
     * @return The pulled value
     */
    double getDouble(String key);

    /**
     * Get the <code>Double</code> value stored at
     * the given key. If the key is not present,
     * the default value is returned.
     *
     * @param key The location to pull the value from
     * @param def The default value
     * @return The pulled value
     */
    double getDouble(String key, double def);

    /**
     * Get the <code>String</code> value stored at
     * the given key
     *
     * @param key The location to pull the value from
     * @return The pulled value
     */
    @Nonnull
    String getString(String key);

    /**
     * Get the <code>String</code> value stored at
     * the given key. If the key is not present,
     * the default value is returned.
     *
     * @param key The location to pull the value from
     * @param def The default value
     * @return The pulled value
     */
    String getString(String key, String def);

    /**
     * Get the <code>boolean</code> value stored at
     * the given key
     *
     * @param key The location to pull the value from
     * @return The pulled value
     */
    boolean getBool(String key);

    /**
     * Get the <code>boolean</code> value stored at
     * the given key. If the key is not present,
     * the default value is returned.
     *
     * @param key The location to pull the value from
     * @param def The default value
     * @return The pulled value
     */
    boolean getBool(String key, boolean def);

    /**
     * Get the <code>List</code> value stored at
     * the given key
     *
     * @param key The location to pull the value from
     * @return The pulled value
     */
    @Nonnull
    List<String> getList(String key);

    /**
     * Get the <code>List</code> value stored at
     * the given key. If the key is not present,
     * the default value is returned.
     *
     * @param key The location to pull the value from
     * @param def The default value
     * @return The pulled value
     */
    List<String> getList(String key, List<String> def);

    /**
     * Get the <code>Object</code> value stored at
     * the given key. If no object is found null is
     * returned
     *
     * @param key The location to pull the value from
     * @return The pulled value
     */
    @Nullable
    Object getObject(String key);

    /**
     * Get the <code>Object</code> value stored at
     * the given key. If the key is not present,
     * the default value is returned.
     *
     * @param key The location to pull the value from
     * @param def The default value
     * @return The pulled value
     */
    Object getObject(String key, Object def);

    /**
     * Get the <code>Object</code> value of type
     * <code>T</code> at the given key
     *
     * @param key The location to pull the value from
     * @param clazz The class used to cast to type T
     * @param <T> The data type of the value
     * @return The pulled and casted value
     */
    @Nullable
    <T> T getObject(String key, Class<T> clazz);

    /**
     * Get the <code>Object</code> value of type
     * <code>T</code> at the given key. If the key
     * is not present, the default value is returned.
     *
     * @param key The location to pull the value from
     * @param def The default value
     * @param clazz The class used to cast to type T
     * @param <T> The data type of the value
     * @return The pulled and casted value
     */
    <T> T getObject(String key, T def, Class<T> clazz);

    /**
     * Checks to see if the given key is contained within
     * this <code>Configuration</code>
     *
     * @param key The key to check for
     * @return true if the key is present
     */
    boolean containsKey(String key);

    /**
     * Checks to see if the given key is contained within
     * this <code>Configuration</code>. If the key is present,
     * then there is a second check to make sure it is of the
     * given data type
     *
     * @param key The key to check for
     * @param clazz The data type to check for
     * @return true if the key is present
     */
    boolean containsKey(String key, Class<?> clazz);

    /**
     * Clears this <code>Configuration</code> by removing
     * every key and value
     */
    void clear();

    /**
     * Loops through every key that starts with
     * <code>path</code> and checks to see if the
     * key is "deep" or not
     *
     * @param path The starting path
     * @param consumer What to do with every key
     * @param deep true if should go deep
     */
    void forEach(String path, BiConsumer<String, Object> consumer, boolean deep);
}
