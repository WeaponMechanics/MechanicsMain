package me.deecaad.core.mechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.Keyable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class Registry<T extends Keyable> implements Keyable {

    private final String registryName;
    private final Map<String, T> registry;

    public Registry(@NotNull String registryName) {
        this.registryName = registryName;
        this.registry = new HashMap<>();
    }

    /**
     * Returns the name of this registry. Serves no functionality outside a
     * user-friendly identifier. The name should be based off of the type this
     * registry holds. For example, <code>"Mechanic"</code> for a registry of
     * mechanics.
     *
     * @return The non-null name of this registry.
     */
    @Override
    public @NotNull String getKey() {
        return registryName;
    }

    /**
     * Adds the given serializer to this registry. Keys are not case-sensitive,
     * so be careful to avoid duplicate keys.
     *
     * @param item The non-null item to add.
     * @return A non-null reference to this (builder-pattern).
     * @throws IllegalArgumentException If a duplicate key is found.
     */
    @Contract("_ -> this")
    @NotNull
    public Registry<T> add(@NotNull T item) {
        String key = toKey(item.getKey());
        T existing = registry.get(key);

        if (existing != null) {
            MechanicsCore.debug.warn("Overriding '" + existing + "' with '" + item + "' in " + registryName + " registry");
        }

        registry.put(key, item);
        return this;
    }

    /**
     * Returns the item associated with the given key, or null.
     *
     * @param key The key to check.
     * @return The serializer associated with the key.
     */
    public @Nullable T get(@NotNull String key) {
        return registry.get(toKey(key));
    }

    /**
     * Returns the optional item associated with the given key.
     *
     * @param key The key to check.
     * @return The optional item.
     */
    public @NotNull Optional<T> getIfPresent(@NotNull String key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * Returns the options available in the registry. This can be passed to a
     * {@link me.deecaad.core.file.SerializerOptionsException} to tell the
     * admin which options are registered and available for usage.
     *
     * @return The non-null set of options.
     */
    public @NotNull Set<String> getOptions() {
        return registry.keySet();
    }

    /**
     * Clears the registry using {@link HashMap#clear()}.
     */
    public void clear() {
        registry.clear();
    }

    /**
     * Keys are use lowercase english letters, and do not include spaces or
     * underscores. This method converts a normal string into a key for a
     * registry.
     *
     * @param key The non-null string to convert.
     * @return The non-null converted key.
     */
    public static String toKey(String key) {
        return key.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "");
    }

    public static boolean matches(String key1, String key2) {
        return toKey(key1).equals(toKey(key2));
    }
}