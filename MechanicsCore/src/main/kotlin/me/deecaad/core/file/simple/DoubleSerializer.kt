package me.deecaad.core.file.simple

import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SimpleSerializer

class DoubleSerializer
    @JvmOverloads
    constructor(
        private val min: Double? = null,
        private val max: Double? = null,
    ) : SimpleSerializer<Double> {
        override fun getTypeName(): String = "floating point number"

        override fun deserialize(
            data: String,
            errorLocation: String,
        ): Double {
            val value = parseSmartPercentage(data, errorLocation)

            if ((min != null && value < min) || (max != null && value > max)) {
                throw SerializerException.Builder()
                    .locationRaw(errorLocation)
                    .buildInvalidRange(value, min, max)
            }

            return value
        }

        @Throws(SerializerException::class)
        private fun parseSmartPercentage(
            input: String,
            errorLocation: String,
        ): Double {
            var s = input.trim()
            var negative = false
            var isPercentage = false

            // Check leading sign (+ or -)
            if (s.startsWith('+')) {
                s = s.substring(1) // remove leading '+'
            } else if (s.startsWith('-')) {
                negative = true
                s = s.substring(1) // remove leading '-'
            }

            if (s.startsWith('%')) {
                isPercentage = true
                s = s.substring(1) // remove leading '%'
            } else if (s.endsWith('%')) {
                isPercentage = true
                s = s.dropLast(1) // remove trailing '%'
            }

            // Attempt to parse the numeric value
            val numericValue =
                s.toDoubleOrNull()
                    ?: throw SerializerException.builder()
                        .locationRaw(errorLocation)
                        .apply { if (isPercentage) example("100%") }
                        .buildInvalidType("double", input)

            val signedValue = if (negative) -numericValue else numericValue

            // If it was indicated as a percentage, divide by 100
            return if (isPercentage) signedValue / 100.0 else signedValue
        }
    }
