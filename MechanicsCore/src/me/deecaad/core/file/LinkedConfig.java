package me.deecaad.core.file;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static me.deecaad.core.MechanicsCore.debug;

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
public class LinkedConfig extends LinkedHashMap<String, Object> implements Configuration {


    @Override
    public void add(ConfigurationSection config) throws DuplicateKeyException {

        List<String> duplicates = new ArrayList<>();

        for (String key : config.getKeys(true)) {

            // Check for duplicate keys
            if (this.containsKey(key)) {
                duplicates.add(key);
            }

            Object value = config.get(key);
            set(key, value);
        }

        // Report duplicate keys
        if (!duplicates.isEmpty()) {
            throw new DuplicateKeyException(duplicates.toArray(new String[0]));
        }
    }

    @Override
    public void add(Configuration config) throws DuplicateKeyException {

        List<String> duplicates = new ArrayList<>();

        config.forEach("", (key, value) -> {

            // Check for duplicate keys
            if (this.containsKey(key)) {
                duplicates.add(key);
            } else {
                set(key, value);
            }
        }, true);

        // Report duplicate keys
        if (!duplicates.isEmpty()) {
            throw new DuplicateKeyException(duplicates.toArray(new String[0]));
        }
    }

    @Nullable
    @Override
    public Object set(String key, Object value) {

        // Convert lists to colored lists of strings
        if (value instanceof List<?>) {
            value = ((List<?>) value).stream()
                    .map(Object::toString)
                    .map(StringUtils::color)
                    .collect(Collectors.toList());
        } else if (value instanceof String) {
            value = StringUtils.color(value.toString());
        }

        // There is no need to cast to specific data types
        // here because they will be casted back to Objects
        return super.put(key, value);
    }

    @Override
    public Set<String> getKeys() {
        return super.keySet();
    }

    @Override
    public int getInt(@Nonnull String key) {
        return ((Number) getOrDefault(key, 0)).intValue();
    }
    
    @Override
    public int getInt(String key, int def) {
        return ((Number) getOrDefault(key, def)).intValue();
    }
    
    @Override
    public double getDouble(@Nonnull String key) {
        return ((Number) getOrDefault(key, 0.0)).intValue();
    }
    
    @Override
    public double getDouble(String key, double def) {
        return ((Number) getOrDefault(key, def)).intValue();
    }
    
    @Override
    public boolean getBool(@Nonnull String key) {
        return (boolean) getOrDefault(key, false);
    }
    
    @Override
    public boolean getBool(String key, boolean def) {
        return (boolean) getOrDefault(key, def);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getList(String key) {
        List<String> value = (List<String>) get(key);

        // Avoid initializing a new Object if
        // we don't need to
        if (value == null) {
            return new ArrayList<>();
        } else {
            return value;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getList(String key, List<String> def) {
        return (List<String>) getOrDefault(key, def);
    }

    @Nullable
    @Override
    public String getString(@Nonnull String key) {
        return (String) getOrDefault(key, "");
    }
    
    @Override
    public String getString(String key, String def) {
        return (String) getOrDefault(key, def);
    }

    /**
     * This implementation has the issue of returning
     * any data type, since every generic is an <code>Object</code>
     *
     * @param key The location to pull the value from
     * @return The object at the given key
     */
    @Nullable
    @Override
    public Object getObject(String key) {
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
        return super.containsKey(key);
    }

    @Override
    public boolean containsKey(String key, Class<?> clazz) {
        Object value = get(key);

        // Check if the value exists
        if (value == null) return false;
        else return clazz.isInstance(value);
    }

    @Override
    public void forEach(@Nonnull String basePath, @Nonnull BiConsumer<String, Object> consumer, boolean deep) {
        int memorySections = StringUtils.countChars('.', basePath);
        forEach((key, value) -> {
            if (key.startsWith(basePath)) {
                int currentMemorySections = StringUtils.countChars('.', basePath);
                if (!deep && currentMemorySections == memorySections + 1) {
                    consumer.accept(key, value);
                } else if (deep && currentMemorySections > memorySections) {
                    consumer.accept(key, value);
                }
            }
        });
    }

    public void save(@Nonnull File file) {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        
        // basic support for changing keys during the program AND
        // allowing the program to change keys.
        try {
            add(configuration);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
        }

        // Deletes all keys from config
        configuration.getKeys(true).forEach(key -> configuration.set(key, null));
        
        // Set and save
        forEach(configuration::set);
        try {
            configuration.save(file);
        } catch (IOException ex) {
            debug.log(LogLevel.ERROR, "Could not save file \"" + file.getName() + "\"", ex);
        }
    }
}
