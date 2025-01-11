package me.deecaad.core.file.simple

import com.github.retrooper.packetevents.protocol.entity.type.EntityType
import com.github.retrooper.packetevents.protocol.item.type.ItemType
import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SimpleSerializer
import org.bukkit.Bukkit
import org.bukkit.Fluid
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Tag
import org.bukkit.block.BlockType

class RegistryValueSerializer<T : Keyed> @JvmOverloads constructor(
    val clazz: Class<T>,
    val isAllowWildcard: Boolean,
    val registry: Registry<T> = Bukkit.getRegistry(clazz)
        ?: throw IllegalArgumentException("Registry for ${clazz.simpleName} does not exist."),
) : SimpleSerializer<List<T>> {

    /**
     * The tag registry associated with the given class, if present.
     */
    val tagRegistry: String? =
        when (clazz) {
            Fluid::class.java -> Tag.REGISTRY_FLUIDS
            EntityType::class.java -> Tag.REGISTRY_ENTITY_TYPES
            else -> null
        }

    override fun getTypeName(): String = clazz.simpleName

    override fun deserialize(
        data: String,
        errorLocation: String,
    ): List<T> {
        var data = data.trim().lowercase()

        // Wildcards, like '$DOOR' will return all values that contain 'door'
        // in their key.
        if (data.startsWith("$")) {
            data = data.substring(1)

            if (!isAllowWildcard) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Wildcards are not allowed here... Remove the wildcard symbol '$'")
                    .addMessage("For value: $data")
                    .build()
            }

            return parseWildcard(data, errorLocation)
        }

        // Some registers have tags, which are like "built-in" wildcards
        if (data.startsWith("#")) {
            data = data.substring(1)

            if (!isAllowWildcard) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Tags are not allowed here... Remove the tag symbol '#'")
                    .addMessage("For value: $data")
                    .build()
            }

            return parseTag(data, errorLocation)
        }

        return listOf(parseOne(data, errorLocation))
    }

    private fun parseOne(data: String, errorLocation: String): T {
        val key =
            NamespacedKey.fromString(data)
                ?: throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("We expect a plain string, like 'dirt' or a namespaced key, like 'minecraft:dirt'.")
                    .buildInvalidType("registry key", data)

        val value = registry[key]
        if (value == null) {
            // before we throw an exception, lets try to match the key
            // to any namespace key in the registry
            val filteredMatches = registry.filter { it.key.key == key.key }
            if (filteredMatches.size == 1) {
                return filteredMatches.first()
            } else if (filteredMatches.size >= 2) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Ambiguous key '$key'.")
                    .addMessage("Found two values with the same key... Please use the full namespaced key.")
                    .example(filteredMatches.map { it.key.toString() }.first())
                    .buildInvalidRegistryOption(data, registry)
            } else {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .buildInvalidRegistryOption(data, registry)
            }
        }

        return value
    }

    private fun parseWildcard(data: String, errorLocation: String): List<T> {
        val values = registry.filter { it.key.key.contains(data.lowercase()) }
        if (values.isEmpty()) {
            throw SerializerException.builder()
                .locationRaw(errorLocation)
                .addMessage("Wildcard '$$data' did not match any values.")
                .addMessage("No values found that contain '$data' in their key.")
                .buildInvalidRegistryOption(data, registry)
        }
        return values
    }

    private fun parseTag(data: String, errorLocation: String): List<T> {
        val key =
            NamespacedKey.fromString(data)
                ?: throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("When using a tag, we expect a plain string, like '#wool' or a namespaced key, like '#minecraft:wool'.")
                    .buildInvalidType("registry key", data)

        // Can't do proper generics here since ItemType is still not finished
        if (clazz == ItemType::class.java) {
            val tag: Tag<Material>? = Bukkit.getTag(Tag.REGISTRY_ITEMS, key, Material::class.java)
            if (tag == null) {
                val tags = Bukkit.getTags(Tag.REGISTRY_ITEMS, Material::class.java)
                val mapped = tags.map { it.key.toString() }
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .buildInvalidOption(data, mapped)
            }
            return tag.values.map { it.asItemType()!! }.toList() as List<T>
        }

        // Can't do proper generics here since BlockType is still not finished
        if (clazz == BlockType::class.java) {
            val tag: Tag<Material>? = Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material::class.java)
            if (tag == null) {
                val tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material::class.java)
                val mapped = tags.map { it.key.toString() }
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .buildInvalidOption(data, mapped)
            }
            return tag.values.map { it.asBlockType()!! }.toList() as List<T>
        }

        if (tagRegistry == null) {
            throw SerializerException.builder()
                .locationRaw(errorLocation)
                .addMessage("Tags are not supported for $typeName")
                .build()
        }

        val tag: Tag<T>? = Bukkit.getTag(tagRegistry, key, clazz)
        if (tag == null) {
            val tags = Bukkit.getTags(tagRegistry, clazz)
            val mapped = tags.map { it.key.toString() }
            throw SerializerException.builder()
                .locationRaw(errorLocation)
                .buildInvalidOption(data, mapped)
        }

        return tag.values.toList()
    }

    override fun examples(): MutableList<String> {
        return registry.map { it.key.toString() }.toMutableList()
    }
}
