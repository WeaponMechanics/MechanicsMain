package me.deecaad.core.file.simple

import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SimpleSerializer
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.Registry

class RegistryValueSerializer<T : Keyed>(
    private val registry: Registry<T>,
    private val isAllowWildcard: Boolean,
) : SimpleSerializer<List<T>> {
    override fun getTypeName(): String {
        // TODO: Make Spigot contribution
        for (item in registry) {
            return item.javaClass.simpleName
        }
        return "unknown register value"
    }

    override fun deserialize(
        data: String,
        errorLocation: String,
    ): List<T> {
        var data = data.trim().lowercase()
        var isWildcard = false

        // Wildcards, like '$DOOR' will return all values that contain 'door'
        // in their key.
        if (data.startsWith("$")) {
            data = data.substring(1)
            isWildcard = true

            if (!isAllowWildcard) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Wildcards are not allowed here... Remove the wildcard symbol '$'")
                    .addMessage("For value: $data")
                    .build()
            }
        }

        val key =
            NamespacedKey.fromString(data)
                ?: throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("We expect a plain string, like 'dirt' or a namespaced key, like 'minecraft:dirt'.")
                    .buildInvalidType("registry key", data)

        if (isWildcard) {
            val values = registry.filter { it.key.key.contains(key.key) }
            if (values.isEmpty()) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Wildcard '$$key' did not match any values.")
                    .addMessage("No values found that contain '$key' in their key.")
                    .buildInvalidRegistryOption(data, registry)
            }
            return values
        }

        val value = registry[key]
        if (value == null) {
            // before we throw an exception, lets try to match the key
            // to any namespace key in the registry
            val filteredMatches = registry.filter { it.key.key == key.key }
            if (filteredMatches.size == 1) {
                return listOf(filteredMatches.first())
            } else if (filteredMatches.size >= 2) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Ambiguous key '$key'.")
                    .addMessage("Found two values with the same key... Please use the full namespaced key.")
                    .example(filteredMatches.map { it.key.toString() }.first())
                    .buildInvalidRegistryOption(data, registry)
            }
        }

        // Nothing else we can do... user probably just made a typo, show normal error
        if (value == null) {
            throw SerializerException.builder()
                .locationRaw(errorLocation)
                .buildInvalidRegistryOption(data, registry)
        }

        return listOf(value)
    }

    override fun examples(): MutableList<String> {
        return registry.map { it.key.toString() }.toMutableList()
    }
}
