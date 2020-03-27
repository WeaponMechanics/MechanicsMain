package me.deecaad.core.file;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.utils.NumberUtils;
import me.deecaad.weaponmechanics.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This class is Map of Strings and Objects that
 * represent different keys and values. All values
 * are put in insertion order, for saving purpose.
 *
 * Since all Objects are stored inside of one map,
 * there is an overheard created from typecasting
 * the objects from their reference type to their
 * Object type.
 *
 * The advantage of this Configuration is that when
 * it is saved, all the keys are saved in order, not
 * disturbing whichever order players may have saved
 * it in.
 *
 * Explanations on each method are provided
 * in the Configuration interface
 *
 * @author cjcrafter
 * @see Configuration
 * @since 1.0
 */
public class OrderedConfig extends LinkedHashMap<String, Object> implements Configuration {
    
    @Override
    public void add(Map<String, ?> map, boolean isIgnoreDuplicates) {
        if (map == null) return;
        
        if (!isIgnoreDuplicates) {
            // This assumes that the map was in order
            new LinkedHashSet<>(map.keySet()).forEach(key -> {
                if (containsKey(key)) {
                    // todo ERROR ERROR - ALERT USER! ah
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
                            // todo ERROR ERROR - ALERT USER! AH
                            return false;
                        } else return true;
                    })
                    .collect(Collectors.toSet());
        }
        keys.forEach(key -> put(key, file.get(key)));
    }
    
    @Override
    public void add(Configuration config) {
        if (config == null) return;
        
        // This assumes duplicate keys were already checked for
        config.forEach("", this::set, true);
    }
    
    @Override
    public void set(String key, Object value) {
        put(key, value);
    }
    
    @Override
    public int getInt(@Nonnull String key) {
        return (int) ((double) getOrDefault(key, 0));
    }
    
    @Override
    public int getInt(String key, int def) {
        return (int) ((double) getOrDefault(key, 0));
    }
    
    @Override
    public double getDouble(@Nonnull String key) {
        return (double) getOrDefault(key, 0.0);
    }
    
    @Override
    public double getDouble(String key, double def) {
        return (double) getOrDefault(key, def);
    }
    
    @Override
    public boolean getBool(@Nonnull String key) {
        return (boolean) getOrDefault(key, false);
    }
    
    @Override
    public boolean getBool(String key, boolean def) {
        return (boolean) getOrDefault(key, def);
    }

    @Nullable
    @Override
    public String getString(@Nonnull String key) {
        Object value = get(key);
        return value != null ? (String) get(key) : null;
    }
    
    @Override
    public String getString(String key, String def) {
        return (String) getOrDefault(key, def);
    }

    @Nullable
    @Override
    public ItemStack getItem(@Nonnull String key) {
        Object value = get(key);
        return value != null ? ((ItemStack) get(key)).clone() : null;
    }
    
    @Override
    public ItemStack getItem(String key, ItemStack def) {
        return ((ItemStack) getOrDefault(key, def)).clone();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getSet(@Nonnull String key) {
        Object value = get(key);
        return value != null ? (Set<String>) get(key) : null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getSet(String key, Set<String> def) {
        return (Set<String>) getOrDefault(key, def);
    }

    @Nullable
    @Override
    public Object getObject(@Nonnull String key) {
        // Here is no casting so it can't try to cast null to some object
        return get(key);
    }
    
    @Override
    public Object getObject(String key, Object def) {
        return getOrDefault(key, def);
    }
    
    @Nullable
    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        // clazz.cast -> returns the object after casting, or null if obj is null
        return clazz.cast(get(key));
    }
    
    @Override
    public <T> T getObject(String key, T def, Class<T> clazz) {
        Object value = get(key);
        return value != null ? clazz.cast(value) : def;
    }
    
    @Override
    public boolean containsKey(@Nonnull String key) {
        return containsKey(key);
    }
    
    @Override
    public void forEach(@Nonnull String basePath, @Nonnull BiConsumer<String, Object> consumer, boolean deep) {
        int memorySections = StringUtils.countChars('.', basePath);
        forEach((key, value) -> {
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
        forEach(configuration::set);
        try {
            configuration.save(file);
        } catch (IOException ex) {
            DebugUtil.log(LogLevel.ERROR, "Could not save file \"" + file.getName() + "\"", ex);
        }
    }
}
