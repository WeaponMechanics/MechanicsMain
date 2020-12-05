package me.deecaad.core.file;

import java.util.Arrays;

/**
 * An <code>Exception</code> thrown when a duplicate
 * key is found in config.
 */
public class DuplicateKeyException extends Exception {

    private final String[] keys;

    public DuplicateKeyException(String... keys) {
        super("Duplicate Key(s): " + Arrays.toString(keys));

        this.keys = keys;
    }

    public String[] getKeys() {
        return keys;
    }
}
