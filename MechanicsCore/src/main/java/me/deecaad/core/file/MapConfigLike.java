package me.deecaad.core.file;

import me.deecaad.core.mechanics.Registry;
import me.deecaad.core.utils.SerializerUtil;
import me.deecaad.core.utils.StringUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapConfigLike implements ConfigLike {

    private final Map<String, Holder> config;
    private File file;
    private String path;
    private String fullLine;

    public MapConfigLike(Map<String, Holder> config) {
        this.config = new HashMap<>();
        for (String key : config.keySet()) {
            String noCase = Registry.toKey(key);

            this.config.put(noCase, config.get(key));
        }
    }

    public MapConfigLike setDebugInfo(File file, String path, String fullLine) {
        this.file = file;
        this.path = path;
        this.fullLine = fullLine;
        return this;
    }

    public File getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }

    public String getFullLine() {
        return fullLine;
    }

    @Override
    public boolean contains(String key) {
        return config.containsKey(Registry.toKey(key));
    }

    @Override
    public Object get(String key, Object def) {
        return config.getOrDefault(Registry.toKey(key), new Holder(def, 0)).value;
    }

    @Override
    public boolean isString(String key) {
        return get(Registry.toKey(key), null) instanceof String;
    }

    @Override
    public List<?> getList(String key) {
        Object temp = get(Registry.toKey(key), null);

        if (temp instanceof List<?> list)
            return list;

        return List.of();
    }

    @Override
    public String getLocation(File localFile, String localPath) {
        Holder holder = config.get(Registry.toKey(localPath));
        if (holder == null)
            return SerializerUtil.foundAt(file, path);

        String indent = "    ";
        return SerializerUtil.foundAt(file, path) + "\n"
            + indent + fullLine + "\n"
            + StringUtil.repeat(" ", indent.length() + holder.index()) + "^";
    }

    public record Holder(Object value, int index) {
    }
}
