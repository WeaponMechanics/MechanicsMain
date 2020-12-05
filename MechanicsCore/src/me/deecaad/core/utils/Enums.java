package me.deecaad.core.utils;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class Enums {

    private static final Map<Class<? extends Enum<?>>, Map<String, WeakReference<? extends Enum<?>>>> cache = new HashMap<>();

    /**
     * Do not let anybody instantiate this class
     */
    private Enums() {
    }

    private static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> getConstants(Class<T> enumClass) {
        synchronized (cache) {
            Map<String, WeakReference<? extends Enum<?>>> temp = cache.get(enumClass);
            if (temp == null)
                return populateEnum(enumClass);

            return temp;
        }
    }

    /**
     * Maps the enums present in <code>enumClass</code> to their
     * <code>String</code> key into <code>cache</code>
     *
     * @param enumClass Enum class to map values into
     * @param <T>       The type of the enum
     * @return The mappings cached by this method
     */
    private static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> populateEnum(Class<T> enumClass) {

        // If that enum has already been cached, then just return
        // the cached mappings -- No need to update the,
        if (cache.containsKey(enumClass))
            return cache.get(enumClass);

        // Temporary mapping to store the enums
        Map<String, WeakReference<? extends Enum<?>>> temp = new HashMap<>();

        for (T enumInstance : EnumSet.allOf(enumClass)) {
            temp.put(enumInstance.name(), new WeakReference<>(enumInstance));
        }

        // Make the temporary mappings immutable so people
        // don't try to add their own mappings
        cache.put(enumClass, Collections.unmodifiableMap(temp));
        return temp;
    }

    /**
     * Optionally returns the enum associated with <code>enumClass</code>
     * with the name <code>name</code>
     *
     * @param enumClass Class to grab the enum from
     * @param name      The name of the enum
     * @param <T>       The enum type
     * @return Optional enum
     */
    public static <T extends Enum<T>> Optional<T> getIfPresent(Class<T> enumClass, String name) {
        WeakReference<? extends Enum<?>> reference = getConstants(enumClass).get(name);
        return reference == null ? Optional.empty() : Optional.of(enumClass.cast(reference.get()));
    }

    /**
     * Returns an immutable set of the names of the enums from
     * <code>enumClass</code>
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
