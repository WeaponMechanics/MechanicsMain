package me.deecaad.core.file;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static me.deecaad.core.MechanicsCore.debug;

/**
 * This class outlines an ordered configuration. Elements are added in
 * insertion order, which may be useful for saving key-value pairs
 * back to hard files.
 *
 * {@link LinkedHashMap} seems to have a faster {@link Map#get(Object)} method
 * then the {@link java.util.HashMap}.
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

        // If we are getting strings, make sure the color formatting has been
        // done.
        if (value instanceof List<?>) {
            value = ((List<?>) value).stream()
                    .map(Object::toString)
                    .map(StringUtil::color)
                    .collect(Collectors.toList());
        } else if (value instanceof String) {
            value = StringUtil.color(value.toString());
        }

        return super.put(key, value);
    }

    @Override
    public @NotNull Set<String> getKeys() {
        return super.keySet();
    }

    @Override
    public int getInt(@NotNull String key) {
        Object value = super.get(key);
        if (!(value instanceof Number)) {
            return 0;
        } else {
            return ((Number) value).intValue();
        }
    }

    @Override
    public int getInt(@NotNull String key, int def) {
        Object value = super.get(key);
        if (!(value instanceof Number)) {
            return def;
        } else {
            return ((Number) value).intValue();
        }
    }

    @Override
    public double getDouble(@NotNull String key) {
        Object value = super.get(key);
        if (!(value instanceof Number)) {
            return 0.0;
        } else {
            return ((Number) value).doubleValue();
        }
    }

    @Override
    public double getDouble(@NotNull String key, double def) {
        Object value = super.get(key);
        if (!(value instanceof Number)) {
            return def;
        } else {
            return ((Number) value).doubleValue();
        }
    }

    @Override
    public boolean getBool(@NotNull String key) {
        Object value = super.get(key);
        if (!(value instanceof Boolean)) {
            return false;
        } else {
            return (Boolean) value;
        }
    }

    @Override
    public boolean getBool(@NotNull String key, boolean def) {
        Object value = super.get(key);
        if (!(value instanceof Boolean)) {
            return def;
        } else {
            return (Boolean) value;
        }
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getList(@NotNull String key) {
        Object value = super.get(key);
        if (!(value instanceof List)) {
            return Collections.emptyList();
        } else {
            return (List<String>) value;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getList(@NotNull String key, List<String> def) {
        Object value = super.get(key);
        if (!(value instanceof List)) {
            return def;
        } else {
            return (List<String>) value;
        }
    }

    @Nullable
    @Override
    public String getString(@NotNull String key) {
        Object value = super.get(key);
        if (!(value instanceof String)) {
            return null;
        } else {
            return (String) value;
        }
    }

    @Override
    public String getString(String key, String def) {
        Object value = super.get(key);
        if (!(value instanceof String)) {
            return def;
        } else {
            return (String) value;
        }
    }

    @Nullable
    @Override
    public Object getObject(@NotNull String key) {
        return super.get(key);
    }

    @Override
    public Object getObject(@NotNull String key, Object def) {
        return super.getOrDefault(key, def);
    }

    @Nullable
    @Override
    public <T> T getObject(@NotNull String key, @NotNull Class<T> clazz) {
        Object value = super.get(key);
        if (!clazz.isInstance(value)) {
            return null;
        } else {
            return clazz.cast(value);
        }
    }

    @Override
    public <T> T getObject(@NotNull String key, T def, @NotNull Class<T> clazz) {
        Object value = super.get(key);
        if (!clazz.isInstance(value)) {
            return def;
        } else {
            return clazz.cast(value);
        }
    }

    @Override
    public boolean containsKey(@NotNull String key) {
        return super.containsKey(key);
    }

    @Override
    public boolean containsKey(@NotNull String key, @NotNull Class<?> clazz) {
        return super.containsKey(key) && clazz.isInstance(get(key));
    }

    @Override
    public void forEach(@NotNull String basePath, @NotNull BiConsumer<String, Object> consumer, boolean deep) {
        int memorySections = StringUtil.countChars('.', basePath);
        if (basePath.isEmpty()) memorySections--;

        // Avoiding lambda for debugging
        for (Map.Entry<String, Object> entry : entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!key.startsWith(basePath)) continue;

            int currentMemorySections = StringUtil.countChars('.', key);
            if (!deep && currentMemorySections == memorySections + 1) {
                consumer.accept(key, value);
            } else if (deep && currentMemorySections > memorySections) {
                consumer.accept(key, value);
            }
        }
    }

    public void save(@NotNull File file) {
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
