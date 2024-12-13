package me.deecaad.core.file

/**
 * A special variation of a serializer exception that occurs when a path-to
 * serializer fails. This specific exception is less often the fault of the
 * user. For plugins hoping to support path-to serialization, this exception
 * should be explicitly caught and handled *after* this first round of
 * serialization.
 */
class PathToSerializerException(
    val serializeData: SerializeData,
    location: String,
    messages: MutableList<String>,
) : SerializerException(location, messages)
