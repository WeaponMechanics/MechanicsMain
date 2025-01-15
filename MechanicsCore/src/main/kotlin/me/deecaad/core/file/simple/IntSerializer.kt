package me.deecaad.core.file.simple

import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SimpleSerializer

class IntSerializer
    @JvmOverloads
    constructor(
        private val min: Int? = null,
        private val max: Int? = null,
    ) : SimpleSerializer<Int> {
        override fun getTypeName(): String = "integer"

        override fun deserialize(
            data: String,
            errorLocation: String,
        ): Int {
            val value =
                data.toIntOrNull()
                    ?: throw SerializerException.Builder()
                        .locationRaw(errorLocation)
                        .buildInvalidType("integer", data)

            if ((min != null && value < min) || (max != null && value > max)) {
                throw SerializerException.Builder()
                    .locationRaw(errorLocation)
                    .buildInvalidRange(value, min, max)
            }

            return value
        }

        override fun examples(): MutableList<String> {
            // Since there are infinite integers, let's just return a couple integers
            // in the correct range
            val min = min ?: 0
            val max = max ?: 100

            return (min..max).map { it.toString() }.toMutableList()
        }
    }
