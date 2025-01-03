package me.deecaad.core.file.simple

import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SimpleSerializer

class ByNameSerializer<T : Any>(
    private val clazz: Class<T>,
    private val byName: Map<String, T>,
) : SimpleSerializer<T> {
    override fun getTypeName(): String = clazz.simpleName

    init {
        byName.keys.forEach {
            require(it.isNotBlank()) { "Key is blank" }
            require(it == it.trim()) { "Key '$it' is not trimmed" }
            require(it == it.lowercase()) { "Key '$it' is not lowercased" }
        }
    }

    override fun deserialize(
        data: String,
        errorLocation: String,
    ): T {
        return byName[data.trim().lowercase()]
            ?: throw SerializerException.builder()
                .locationRaw(errorLocation)
                .buildInvalidOption(data, byName.keys)
    }

    override fun examples(): MutableList<String> {
        return byName.keys.toMutableList()
    }
}
