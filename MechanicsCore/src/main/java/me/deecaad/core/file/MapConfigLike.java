package me.deecaad.core.file;

import java.util.List;
import java.util.Map;

public class MapConfigLike implements ConfigLike {

    private final Map<String, Object> config;

    public MapConfigLike(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public boolean contains(String key) {
        return config.containsKey(key);
    }

    @Override
    public Object get(String key, Object def) {
        return config.getOrDefault(key, def);
    }

    @Override
    public boolean isString(String key) {
        return config.get(key) instanceof String;
    }

    @Override
    public List<Object> getStringList(String key) {
        return ;
    }
}
