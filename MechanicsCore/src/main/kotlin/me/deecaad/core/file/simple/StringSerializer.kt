package me.deecaad.core.file.simple

import me.deecaad.core.file.SimpleSerializer

class StringSerializer : SimpleSerializer<String> {
    override fun getTypeName(): String = "string"

    override fun deserialize(
        data: String,
        errorLocation: String,
    ): String {
        return data
    }
}
