package me.deecaad.core.file.simple

import me.deecaad.core.file.SerializerException

class BooleanSerializer : SimpleSerializer<Boolean> {
    override val typeName: String = "true/false"

    override fun deserialize(data: String, errorLocation: String): Boolean {
        return when (data.lowercase()) {
            "true" -> true
            "false" -> false
            else -> throw SerializerException.Builder()
                .locationRaw(errorLocation)
                .buildInvalidType("true/false", data)
        }
    }
}
