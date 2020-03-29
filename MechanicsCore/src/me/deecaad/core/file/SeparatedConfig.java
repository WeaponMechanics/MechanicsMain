package me.deecaad.core.file;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This class separates "common" reference
 * types into separate maps to avoid the
 * overhead from typecasting, and to improve
 * the readability.
 *
 * Explanations on each method are provided
 * in the Configuration interface
 *
 * @author cjcrafter
 * @see Configuration
 * @since 1.0
 */
public class SeparatedConfig implements Configuration {
    
    private final Set<Map<String, ?>>      maps;
    private final Map<String, Number>       numbers;
    private final Map<String, Boolean>      booleans;
    private final Map<String, String>       strings;
    private final Map<String, ItemStack>    items;
    private final Map<String, Set<String>> lists;
    private final Map<String, Object>       objects;
    
    /**
     * Initializes all maps and adds them
     * into the list of maps.
     *
     * May be worth making these LinkedHashMaps,
     * but the ram usage effect should be looked
     * into before changing.
     */
    public SeparatedConfig() {
        maps = new HashSet<>();
        maps.add(numbers = new HashMap<>());
        maps.add(booleans = new HashMap<>());
        maps.add(strings = new HashMap<>());
        maps.add(items = new HashMap<>());
        maps.add(lists = new HashMap<>());
        maps.add(objects = new HashMap<>());
    }
    
    @Override
    public void add(Map<String, ?> map, boolean isIgnoreDuplicates) {
        if (map == null) return;
        
        if (!isIgnoreDuplicates) {
            // This assumes that the map was in order
            new LinkedHashSet<>(map.keySet()).forEach(key -> {
                if (containsKey(key)) {
                    DebugUtil.log(LogLevel.ERROR, "Duplicate key \"" + key + "\"");
                    map.remove(key);
                }
            });
        }
        map.forEach(this::set);
    }
    
    @Override
    public void add(ConfigurationSection file, boolean isIgnoreDuplicates) {
        if (file == null) return;
        
        // Returns a LinkedHashSet (Retaining order)
        Set<String> keys = file.getKeys(true);
        
        if (!isIgnoreDuplicates) {
            keys = keys.stream()
                    .filter(key -> {
                        if (containsKey(key)) {
                            DebugUtil.log(LogLevel.ERROR, "Duplicate key \"" + key + "\" found in " + file.getName());
                            return false;
                        } else return true;
                    })
                    .collect(Collectors.toSet());
        }
        keys.forEach(key -> set(key, file.get(key)));
    }
    
    @Override
    public void add(Configuration config) {
        if (config == null) return;
        
        // This assumes duplicate keys were already checked for
        config.forEach("", this::set, true);
    }
    
    @Override
    public void set(String key, Object value) {
        if      (value instanceof Number)   numbers.put(key, (Number) value);
        else if (value instanceof Boolean)  booleans.put(key, (Boolean) value);
        else if (value instanceof String)   strings.put(key, (String) value);
        else if (value instanceof ItemStack)items.put(key, (ItemStack) value);
        else if (value instanceof Set<?>)  lists.put(key, convertSet(value));
        else                                objects.put(key, value);
    }
    
    /**
     * Converts an object to a list of colored strings
     *
     * @param obj The list
     * @return The converted list
     */
    private Set<String> convertSet(Object obj) {
        return ((Set<?>) obj).stream()
                .map(object -> StringUtils.color(object.toString()))
                .collect(Collectors.toSet());
    }
    
    @Override
    public int getInt(String key) {
        return numbers.getOrDefault(key, 0).intValue();
    }
    
    @Override
    public int getInt(String key, int def) {
        return numbers.getOrDefault(key, def).intValue();
    }
    
    @Override
    public double getDouble(String key) {
        return numbers.getOrDefault(key, 0.0).doubleValue();
    }
    
    @Override
    public double getDouble(String key, double def) {
        return numbers.getOrDefault(key, def).doubleValue();
    }
    
    @Override
    public boolean getBool(String key) {
        return booleans.getOrDefault(key, false);
    }
    
    @Override
    public boolean getBool(String key, boolean def) {
        return booleans.getOrDefault(key, def);
    }
    
    @Nonnull
    @Override
    public String getString(String key) {
        return strings.getOrDefault(key, "");
    }
    
    @Override
    public String getString(String key, String def) {
        return strings.getOrDefault(key, def);
    }
    
    @Nonnull
    @Override
    public ItemStack getItem(String key) {
        return items.getOrDefault(key, new ItemStack(Material.AIR));
    }
    
    @Override
    public ItemStack getItem(String key, ItemStack def) {
        return items.getOrDefault(key, def);
    }
    
    @Nonnull
    @Override
    public Set<String> getSet(String key) {
        return lists.getOrDefault(key, new HashSet<>());
    }
    
    @Override
    public Set<String> getSet(String key, Set<String> def) {
        return lists.getOrDefault(key, def);
    }
    
    @Nonnull
    @Override
    public Object getObject(String key) {
        return objects.getOrDefault(key, new Object());
    }
    
    @Override
    public Object getObject(String key, Object def) {
        return objects.getOrDefault(key, def);
    }
    
    @Nullable
    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        if (objects.get(key) != null)
            return clazz.cast(objects.get(key));
        return null;
    }
    
    @Override
    public <T> T getObject(String key, T def, Class<T> clazz) {
        if (objects.get(key) != null)
            return clazz.cast(objects.get(key));
        return def;
    }
    
    @Override
    public boolean containsKey(@Nonnull String key) {
        return maps.stream().anyMatch(map -> map.containsKey(key));
    }
    
    @Override
    public void clear() {
        maps.forEach(Map::clear);
    }
    
    @Override
    public void forEach(@Nonnull String basePath, @Nonnull BiConsumer<String, Object> consumer, boolean deep) {
        maps.forEach(map -> forEach(map, basePath, consumer, deep));
    }
    
    private void forEach(Map<String, ?> map, String basePath, BiConsumer<String, Object> consumer, boolean deep) {
        int memorySections = StringUtils.countChars('.', basePath);
        map.forEach((key, value) -> {
            if (key.startsWith(basePath)) {
                int currentMemorySections = StringUtils.countChars('.', basePath);
                if (deep && currentMemorySections == memorySections + 1) {
                    consumer.accept(key, value);
                } else if (!deep && currentMemorySections > memorySections) {
                    consumer.accept(key, value);
                }
            }
        });
    }
    
    @Override
    public void save(@Nonnull File file) {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    
        // basic support for changing keys during the program AND
        // allowing the program to change keys.
        add(configuration, true);
    
        // Deletes all keys from config
        configuration.getKeys(true).forEach(key -> configuration.set(key, null));
    
        // Set and save
        maps.forEach(map -> map.forEach(configuration::set));
        try {
            configuration.save(file);
        } catch (IOException ex) {
            DebugUtil.log(LogLevel.ERROR, "Could not save file \"" + file.getName() + "\"", ex);
        }
    }
}