package me.deecaad.core.file

import me.deecaad.core.utils.Debugger
import me.deecaad.core.utils.EnumUtil
import me.deecaad.core.utils.LogLevel
import me.deecaad.core.utils.StringUtil
import java.io.File
import java.util.stream.Stream
import kotlin.math.abs

/**
 * An exception that is thrown when a mistake is found in the config file.
 * These mistakes are typically caused by the user, and not the plugin itself.
 *
 * Serializer exceptions are designed to be caught by the plugin during the
 * deserialization process, and then logged to the console. This way, users
 * see very "pretty" error messages instead of ugly stack traces. This makes
 * it more possible for people to fix their own errors.
 *
 * @param location A human-readable location of the mistake (file location + path, generally)
 * @param messages A list of messages that describe the mistake in more detail
 */
open class SerializerException(
    private val location: String,
    private val messages: MutableList<String> = mutableListOf()
) : Exception() {

    @JvmOverloads
    fun log(debug: Debugger, level: LogLevel = LogLevel.ERROR) {
        val fullError = mutableListOf<String>()
        fullError.add("A mistake was found in your config!")
        fullError.addAll(messages)
        fullError.add(location)
        fullError.add("")  // An empty line to separate the error from the rest of the log

        debug.log(level, fullError)
    }

    class Builder {
        private var location: String? = null
        private val messages: MutableList<String> = mutableListOf()

        fun location(
            file: File,
            path: String,
            index: Int? = null
        ): Builder {
            this.location = when {
                index != null -> "Located in file '$file' at '$path' (The ${StringUtil.ordinal(index)} list item)"
                else -> "Located in file '$file' at '$path'"
            }
            return this
        }

        fun locationRaw(location: String): Builder {
            this.location = location
            return this
        }

        fun example(exampleValue: String): Builder {
            messages.add("Example value: $exampleValue")
            return this
        }

        fun addMessage(message: String): Builder {
            messages.add(message)
            return this
        }

        fun didYouMean(actual: String, options: Iterable<String>) {
            val expected = StringUtil.didYouMean(actual, options)
            messages.add("Did you mean to use '$expected' instead of '$actual'?")
        }

        fun possibleValues(actual: String, options: Iterable<String>, count: Int): Builder {
            val arr = options.toList()
            val actualTable = StringUtil.toCharTable(actual)

            val sortedArr = arr.sortedWith { a, b ->
                val aTable = StringUtil.toCharTable(a)
                val bTable = StringUtil.toCharTable(b)

                var diffA = abs(actual.length - a.length)
                var diffB = abs(actual.length - b.length)

                for (i in actualTable.indices) {
                    diffA += abs(actualTable[i] - aTable[i])
                    diffB += abs(actualTable[i] - bTable[i])
                }

                diffA.compareTo(diffB)
            }

            val limitedCount = minOf(sortedArr.size, count)
            val builder = StringBuilder("Showing ")

            if (limitedCount == sortedArr.size) {
                builder.append("All")
            } else {
                builder.append("$limitedCount/${sortedArr.size}")
            }

            builder.append(" Options:")

            if (limitedCount > 0) {
                builder.append(sortedArr.take(limitedCount).joinToString("") { " '$it'" })
            }

            messages.add(builder.toString())
            return this
        }

        fun buildInvalidRange(actual: Int, min: Int?, max: Int?): SerializerException {
            if (location == null) {
                throw IllegalStateException("Location must be set before calling buildInvalidRange")
            }

            messages.add("Invalid range! Expected a value between ${min ?: "-∞"} and ${max ?: "∞"}")
            messages.add("Found value: $actual")

            return SerializerException(location!!, messages)
        }

        fun buildInvalidRange(actual: Double, min: Double?, max: Double?): SerializerException {
            if (location == null) {
                throw IllegalStateException("Location must be set before calling buildInvalidRange")
            }

            messages.add("Invalid range! Expected a value between ${min ?: "-∞"} and ${max ?: "∞"}")
            messages.add("Found value: $actual")

            return SerializerException(location!!, messages)
        }

        fun buildInvalidRegistryOption(input: String, registry: org.bukkit.Registry<*>): SerializerException {
            return buildInvalidOption(input, registry.map { it.key.key })
        }

        fun <T : Enum<T>> buildInvalidEnumOption(input: String, enumClass: Class<T>): SerializerException {
            return buildInvalidOption(input, EnumUtil.getOptions(enumClass))
        }

        fun buildInvalidOption(input: String, options: Iterable<String>): SerializerException {
            if (location == null) {
                throw IllegalStateException("Location must be set before calling buildInvalidOption")
            }

            messages.add("Unknown value '$input'")
            didYouMean(input, options)
            possibleValues(input, options, 5)

            return SerializerException(location!!, messages)
        }

        fun buildMissingRequiredKey(missingKey: String): SerializerException {
            if (location == null) {
                throw IllegalStateException("Location must be set before calling buildMissingRequiredKey")
            }

            messages.add("Missing required key '$missingKey'")
            messages.add("Make sure you spell it correctly (case sensitive)")

            return SerializerException(location!!, messages)
        }

        fun buildInvalidType(expectedTyped: String, actualValue: Any) : SerializerException {
            if (location == null) {
                throw IllegalStateException("Location must be set before calling buildInvalidType")
            }

            messages.add("Invalid type! Expected a $expectedTyped")
            messages.add("Found value: $actualValue")

            return SerializerException(location!!, messages)
        }

        fun build() : SerializerException {
            if (location == null) {
                throw IllegalStateException("Location must be set before calling build")
            }

            return SerializerException(location!!, messages)
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}