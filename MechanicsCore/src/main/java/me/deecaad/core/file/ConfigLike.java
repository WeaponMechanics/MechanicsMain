package me.deecaad.core.file;

import java.io.File;
import java.util.List;

public interface ConfigLike {

    boolean contains(String key);

    default Object get(String key) {
        return get(key, null);
    }

    Object get(String key, Object def);

    boolean isString(String key);

    default String getString(String key) {
        Object temp = get(key);
        if (temp == null)
            return null;

        return temp.toString();
    }

    List<?> getList(String key);

    String getLocation(File localFile, String localPath);
}
