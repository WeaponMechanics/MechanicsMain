package me.deecaad.core.file;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;
import java.util.Map;
import java.util.function.BiConsumer;

public interface Configuration {
    
    /**
     * Should add all keys and values from the map into
     * the configuration.
     *
     * @param map The map to add
     * @param isIgnoreDuplicates Whether or not to ignore duplicate keys
     */
    void add(Map<String, ?> map, boolean isIgnoreDuplicates);
    
    /**
     * Should add all keys and values from the file
     * into the configuration.
     *
     * @param file The file to add
     * @param isIgnoreDuplicates Whether or not to ignore duplicate keys
     */
    void add(ConfigurationSection file, boolean isIgnoreDuplicates);
    
    /**
     * Should copy all keys and values from the given
     * configuration into this configuration.
     *
     * @param config The config to add
     */
    void add(Configuration config);
    
    /**
     * Sets the given value at the given location
     *
     * @param key Location
     * @param value The value
     */
    void set(String key, Object value);
    
    /**
     * Gets the Integer at the given location
     *
     * @param key Location
     * @return Integer
     */
    int getInt(String key);
    int getInt(String key, int def);
    
    /**
     * Gets the Double at the given location
     *
     * @param key Location
     * @return Double
     */
    double getDouble(String key);
    double getDouble(String key, double def);
    
    /**
     * Gets the Boolean at the given location
     *
     * @param key Location
     * @return Boolean
     */
    boolean getBool(String key);
    boolean getBool(String key, boolean def);
    
    /**
     * Gets the String at the given location
     *
     * @param key Location
     * @return String
     */
    @Nonnull
    String getString(String key);
    String getString(String key, String def);
    
    /**
     * Returns the ItemStack at the given location
     *
     * @param key Location
     * @return ItemStack
     */
    @Nonnull
    ItemStack getItem(String key);
    ItemStack getItem(String key, ItemStack def);
    
    /**
     * Returns the Set at the given location
     *
     * @param key Location
     * @return Set
     */
    @Nonnull
    Set<String> getSet(String key);
    Set<String> getSet(String key, Set<String> def);
    
    /**
     * Returns the object at the given location
     *
     * @param key Location
     * @return Object
     */
    @Nonnull
    Object getObject(String key);
    Object getObject(String key, Object def);
    
    /**
     * Attempts to cast the object at the
     * given location to the given class type.
     *
     * @param key Location
     * @param clazz The class to cast to
     * @param <T> The class' type to cast to
     * @return The casted object
     */
    @Nullable
    <T> T getObject(String key, Class<T> clazz);
    <T> T getObject(String key, T def, Class<T> clazz);
    
    /**
     * Tests whether or not configuration contains the
     * given key
     *
     * @param key The key to test
     * @return Whether or not config contains the key
     */
    boolean containsKey(@Nonnull String key);
    
    /**
     * Removed all keys from the map
     */
    void clear();
    
    /**
     * Loops through every key, where every key is defined as
     * any key that contains <code>basePath</code>. The boolean
     * <code>deep</code> dictates how many memory sections deeper
     * then basePath the function should check
     *
     * Ex) forEach("Weapon.Shoot", ..., false)
     *   Weapon:
     *     Shoot:
     *       Spread: # Loops
     *         Horizontal: # Does not
     *         Vertical: # Does not
     *       Test: # Loops
     *       Hey: # Loops
     *
     * Ex) forEach("", ..., true)
     *   Weapon: # Loops
     *     Shoot: # Loops
     *       Spread: # Loops
     *       Test: # Loops
     *
     * This overall works very similar to <code>FileConfiguration</code>'s
     * getKeys(deep).forEach(...)
     *
     * todo Compare different ways to loop https://stackoverflow.com/questions/46898/how-do-i-efficiently-iterate-over-each-entry-in-a-java-map?rq=1
     *
     * @see FileConfiguration
     *
     * @param basePath Where to "start" the loop
     * @param consumer The action to perform
     * @param deep Whether or not to check keys past a memory section
     */
    void forEach(@Nonnull String basePath, @Nonnull BiConsumer<String, Object> consumer, boolean deep);
    
    /**
     * Attempts to save the configuration
     * to the given file
     *
     * @param file The file to save to
     */
    void save(@Nonnull File file);
}
