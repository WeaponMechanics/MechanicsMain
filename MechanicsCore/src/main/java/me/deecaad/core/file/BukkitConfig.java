package me.deecaad.core.file;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class BukkitConfig implements ConfigLike {

    private final ConfigurationSection config;

    public BukkitConfig(ConfigurationSection config) {
        this.config = config;
    }

    @Override
    public boolean contains(String key) {
        return config.contains(key);
    }

    @Override
    public Object get(String key, Object def) {
        return config.get(key, def);
    }

    @Override
    public boolean isString(String key) {
        return config.isString(key);
    }

    @Override
    public List<String> getStringList(String key) {
        return config.getStringList(key);
    }
}
