package me.deecaad.core.utils

import java.io.File

/**
 * A utility class for serializer exceptions.
 */
object SerializerUtil {

    /**
     * Returns a string that describes the location of a file in a directory.
     *
     * @param file The file to describe.
     * @param path The path of the file.
     * @param index The index of the file in a list.
     * @return A string that describes the location of a file in a directory.
     */
    @JvmStatic
    @JvmOverloads
    fun foundAt(file: File, path: String, index: Int? = null): String {
        return when {
            index != null -> "Located in file '$file' at '$path' (The ${StringUtil.ordinal(index)} list item)"
            else -> "Located in file '$file' at '$path'"
        }
    }

    /**
     * Returns the name of the most similar enum to the `input`.
     *
     * @param T The enum type.
     * @param input The input to compare.
     * @param enum The enum class.
     * @return The name of the most similar enum to the `input`.
     */
    @JvmStatic
    fun <T : Enum<T>> didYouMeanEnum(input: String, enum: Class<T>): String {
        return "Did you mean ${StringUtil.didYouMean(input, EnumUtil.getOptions(enum))} instead of $input?"
    }
}