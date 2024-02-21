package me.deecaad.core.file;

import me.deecaad.core.utils.SerializerUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
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
    public List<?> getList(String key) {
        return config.getList(key);
    }

    @Override
    public String getLocation(File localFile, String localPath) {
        return SerializerUtil.foundAt(localFile, localPath);
    }
}
