package me.deecaad.core.file.simple

import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SimpleSerializer

class DoubleSerializer @JvmOverloads constructor(
    private val min: Double? = null,
    private val max: Double? = null,
) : SimpleSerializer<Double> {
    override fun getTypeName(): String = "floating point number"

    override fun deserialize(data: String, errorLocation: String): Double {
        val value = data.toDoubleOrNull()
            ?: throw SerializerException.Builder()
                .locationRaw(errorLocation)
                .buildInvalidType("double", data)

        if ((min != null && value < min) || (max != null && value > max)) {
            throw SerializerException.Builder()
                .locationRaw(errorLocation)
                .buildInvalidRange(value, min, max)
        }

        return value
    }
}