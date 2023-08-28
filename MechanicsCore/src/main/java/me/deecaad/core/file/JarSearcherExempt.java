package me.deecaad.core.file;

/**
 * Any class that implements this interface will not be included in the results
 * for a {@link me.deecaad.core.file.JarSearcher}. This is done for classes
 * with dependencies, like MythicMobs conditions, which require MythicMobs to
 * be installed.
 */
public interface JarSearcherExempt {
}
