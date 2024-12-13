package me.deecaad.core.file.simple

/**
 * A serializer that can parse objects from strings. The serializer is "simple"
 * because the string is expected in a human-readable format. This means no
 * JSON, XML, or other complex formats. A value more like "DIRT" or "5" is
 * expected.
 *
 * @param T The type of object to parse.
 */
interface SimpleSerializer<T : Any> {

    /**
     * The name of the type of the serializer. This is used for error messages.
     */
    val typeName: String

    /**
     * Parses an object from a string.
     *
     * @param data The string to parse.
     * @param errorLocation The location of the error in the file.
     * @return The parsed object.
     */
    fun deserialize(data: String, errorLocation: String): T
}
