package me.deecaad.core.file.simple

import me.deecaad.core.file.SimpleSerializer

class CsvSerializer<T : Any, S : SimpleSerializer<T>>(
    private val serializer: S,
) : SimpleSerializer<List<T>> {
    override fun getTypeName(): String {
        return "${serializer.typeName}1, ${serializer.typeName}2, ${serializer.typeName}3, ..."
    }

    override fun deserialize(
        data: String,
        errorLocation: String,
    ): List<T> {
        return data.split(", ?".toRegex()).map { serializer.deserialize(it, errorLocation) }
    }

    override fun examples(): MutableList<String> {
        // Since the serializer's examples may be an infinite set, let's
        // just return an iterator with 1 example (the first 3 elements of the set)
        var examples = serializer.examples().asSequence().take(3)

        // if there are not enough samples, repeat
        while (examples.count() < 2) {
            examples += examples
        }

        return mutableListOf(examples.joinToString(", "))
    }
}
