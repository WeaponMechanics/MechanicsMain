package me.deecaad.core.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * This final utility class outlines static methods that operate on or return
 * enums. This class keeps a cache of enums to save CPU processes at the
 * expense of increase memory usage.
 *
 * <p>The methods in this class are designed to be threadsafe.
 */
public final class EnumUtil {

    private static final Map<Class<? extends Enum<?>>, Map<String, WeakReference<? extends Enum<?>>>> cache = new HashMap<>();

    // Don't let anyone instantiate this class
    private EnumUtil() {
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> Map<String, WeakReference<T>> getConstants(Class<T> enumClass) {

        // Obtaining a lock is probably unavoidable
        synchronized (cache) {
            Map<String, WeakReference<T>> temp = (Map<String, WeakReference<T>>) (Map<?, ?>) cache.get(enumClass);
            if (temp == null)
                return populateEnum(enumClass);

            return temp;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> Map<String, WeakReference<T>> populateEnum(Class<T> clazz) {

        if (cache.containsKey(clazz))
            return (Map<String, WeakReference<T>>) (Map<?, ?>) cache.get(clazz);

        // Temporary mapping to store the enums, linked for a predictable
        // ordering of values.
        Map<String, WeakReference<? extends Enum<?>>> temp = new LinkedHashMap<>();

        for (T enumInstance : EnumSet.allOf(clazz)) {
            temp.put(enumInstance.name(), new WeakReference<>(enumInstance));
        }

        // Make the temporary mappings immutable so people
        // don't try to add their own mappings
        cache.put(clazz, Collections.unmodifiableMap(temp));
        return (Map<String, WeakReference<T>>) (Map<?, ?>) temp;
    }

    /**
     * Returns an immutable list of enum values that match the input. If
     * the <code>input</code> starts with a <code>$</code>, all enum values that
     * contain the input are added to the list.
     *
     * <p>Otherwise, this method returns an immutable list of 0 or 1 enums values.
     *
     * @param input The input matcher. If the input starts with a $, it matches
     *              multiple enum values.
     * @return A non-null, immutable list of all parsed enum values.
     * @see Collections#unmodifiableList(List)
     * @see Collections#singletonList(Object)
     * @see Collections#emptyList()
     */
    public static <T extends Enum<T>> List<T> parseEnums(Class<T> clazz, String input) {
        input = input.trim().toUpperCase(Locale.ROOT);

        if (input.startsWith("$")) {
            List<T> list = new ArrayList<>();
            String base = input.substring(1);

            for (String enumValue : EnumUtil.getOptions(clazz)) {
                if (enumValue.contains(base)) {
                    list.add(Enum.valueOf(clazz, enumValue));
                }
            }
            return Collections.unmodifiableList(list);
        } else {
            Optional<T> enumValue = EnumUtil.getIfPresent(clazz, input);
            return enumValue.map(Collections::singletonList).orElse(Collections.emptyList());
        }
    }

    /**
     * Returns an {@link Enum} of the given {@link Class} type as an
     * {@link Optional}. The resulting {@link Optional} will be empty if no
     * {@link Enum} with the <code>name</code> exists.
     *
     * @param clazz The enum's non-null class type.
     * @param name  The name of the enum to grab. This string should be trimmed
     *              ({@link String#trim()}) and uppercase
     *              ({@link String#toUpperCase()}).
     * @param <T>   The generic type of the enum.
     * @return An optional of the enum found, or an empty optional.
     */
    public static <T extends Enum<T>> @NotNull Optional<T> getIfPresent(@NotNull Class<T> clazz, @NotNull String name) {
        WeakReference<? extends Enum<?>> reference = getConstants(clazz).get(name.trim().toUpperCase(Locale.ROOT));
        return reference == null ? Optional.empty() : Optional.of(clazz.cast(reference.get()));
    }

    /**
     * Returns an {@link Enum} of the given {@link Class}, or returns null.
     *
     * @param clazz The enum's non-null class type.
     * @param name  The name of the enum to grab. This string should be trimmed
     *              ({@link String#trim()}) and uppercase
     *              ({@link String#toUpperCase()}).
     * @param <T>   The generic type of the enum.
     * @return The enum found, or null.
     */
    public static <T extends Enum<T>> @Nullable T getOrNull(@NotNull Class<T> clazz, @NotNull String name) {
        return getIfPresent(clazz, name).orElse(null);
    }

    /**
     * Returns an immutable set of the name of every {@link Enum} associated
     * with the given enum class.
     *
     * @param clazz Class to grab the enum from.
     * @param <T>   The enum type.
     * @return immutable set of all enums.
     * @see Collections#unmodifiableMap(Map)
     */
    public static <T extends Enum<T>> @NotNull Set<String> getOptions(@NotNull Class<T> clazz) {
        Map<String, WeakReference<T>> temp = getConstants(clazz);
        return temp.keySet();
    }

    /**
     * Returns an immutable collection of the {@link WeakReference} to every
     * {@link Enum} associated with the given enum class. The references will
     * only return null if garbage collections has unloaded the enums.
     *
     * @param clazz Class to grab the enum from.
     * @param <T>   The enum type.
     * @return An immutable set of all enums.
     * @see Collections#unmodifiableMap(Map)
     */
    public static <T extends Enum<T>> @NotNull List<T> getValues(@NotNull Class<T> clazz) {
        Map<String, WeakReference<T>> temp = getConstants(clazz);

        List<T> list = new ArrayList<>(temp.size());
        for (WeakReference<T> reference : temp.values()) {
            list.add(reference.get());
        }

        return list;
    }
}
