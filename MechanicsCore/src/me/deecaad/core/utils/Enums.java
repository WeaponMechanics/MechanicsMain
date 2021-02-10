package me.deecaad.core.utils;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This final utility class outlines static methods that operate on or return
 * enums. This class keeps a cache of enums to save CPU processes at the
 * expense of increase memory usage.
 *
 * <p>The methods in this class are designed to be threadsafe.
 */
public final class Enums {

    private static final Map<Class<? extends Enum<?>>, Map<String, WeakReference<? extends Enum<?>>>> cache = new HashMap<>();

    // Don't let anyone instantiate this class
    private Enums() {
    }

    private static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> getConstants(Class<T> enumClass) {

        // Obtaining a lock is probably unavoidable
        synchronized (cache) {
            Map<String, WeakReference<? extends Enum<?>>> temp = cache.get(enumClass);
            if (temp == null)
                return populateEnum(enumClass);

            return temp;
        }
    }

    private static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> populateEnum(Class<T> clazz) {

        if (cache.containsKey(clazz))
            return cache.get(clazz);

        // Temporary mapping to store the enums
        Map<String, WeakReference<? extends Enum<?>>> temp = new HashMap<>();

        for (T enumInstance : EnumSet.allOf(clazz)) {
            temp.put(enumInstance.name(), new WeakReference<>(enumInstance));
        }

        // Make the temporary mappings immutable so people
        // don't try to add their own mappings
        cache.put(clazz, Collections.unmodifiableMap(temp));
        return temp;
    }

    /**
     * Returns an {@link Enum} of the given {@link Class} type as an
     * {@link Optional}. The resulting {@link Optional} will be empty if no
     * {@link Enum} with the <code>name</code> exists.
     *
     * @param clazz The enum's non-null class type. This is generally a static
     *              class reference.
     * @param name  The name of the enum to grab. This string should be trimmed
     *              ({@link String#trim()}) and uppercase
     *              ({@link String#toUpperCase()}).
     * @param <T>   The generic type of the enum.
     * @return An optional of the enum found, or an empty optional.
     */
    public static <T extends Enum<T>> Optional<T> getIfPresent(Class<T> clazz, String name) {
        WeakReference<? extends Enum<?>> reference = getConstants(clazz).get(name);
        return reference == null ? Optional.empty() : Optional.of(clazz.cast(reference.get()));
    }

    /**
     * Returns an immutable set of the name of every {@link Enum} associated
     * with the given enum class.
     *
     * @param enumClass Class to grab the enum from
     * @param <T>       The enum type
     * @return immutable set of all enums
     * @see Collections#unmodifiableMap(Map)
     */
    public static <T extends Enum<T>> Set<String> getOptions(Class<T> enumClass) {
        Map<String, WeakReference<? extends Enum<?>>> temp = getConstants(enumClass);
        return temp.keySet();
    }
}
