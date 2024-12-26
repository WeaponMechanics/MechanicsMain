package me.deecaad.core.file.simple

import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SimpleSerializer

class BooleanSerializer : SimpleSerializer<Boolean> {
    override fun getTypeName(): String = "true/false"

    override fun deserialize(
        data: String,
        errorLocation: String,
    ): Boolean {
        return when (data.lowercase()) {
            "true" -> true
            "false" -> false
            else -> throw SerializerException.Builder()
                .locationRaw(errorLocation)
                .buildInvalidType("true/false", data)
        }
    }
}
