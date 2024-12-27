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
                    .addMessage("Wildcards are now allowed here... Remove the wildcard symbol '$'")
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
        } else {
            val value =
                registry[key]
                    ?: throw SerializerException.builder()
                        .locationRaw(errorLocation)
                        .buildInvalidRegistryOption(data, registry)

            return listOf(value)
        }
    }

    override fun examples(): MutableList<String> {
        return registry.map { it.key.toString() }.toMutableList()
    }
}
