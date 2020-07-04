package me.deecaad.core.file.storage;

import java.util.Map;

/**
 * Any class that needs to save data to files
 * should implement this saveable
 */
public interface FileSaveable {

    /**
     * Map of all data to save, string key,
     * object value
     *
     * @return Data to save
     */
    Map<String, Object> getData();

    /**
     * Gets the path location to save the data
     * defined by <code>getData</code>. For example,
     * if this is a <code>Player</code>, then this should
     * be it's <code>UUID</code>
     *
     * @return Location to save data
     */
    String getPath();

}
