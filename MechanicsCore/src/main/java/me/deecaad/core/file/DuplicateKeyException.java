package me.deecaad.core.file;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;

/**
 * This class outlines an {@link Exception} that occurs when 2 configuration files with a matching
 * key are added to the same {@link Configuration}
 *
 * @see Configuration#add(Configuration)
 * @see Configuration#add(ConfigurationSection)
 */
public class DuplicateKeyException extends Exception {

    private final String[] keys;

    public DuplicateKeyException(String... keys) {
        super("Duplicate Key(s): " + Arrays.toString(keys));

        this.keys = keys;
    }

    /**
     * Returns an array of all of the matching keys. The returned array will never be <code>null</code>,
     * it will have at least 1 element, and none of the elements will be <code>null</code>.
     *
     * @return The matching keys.
     */
    public String[] getKeys() {
        return keys;
    }
}
