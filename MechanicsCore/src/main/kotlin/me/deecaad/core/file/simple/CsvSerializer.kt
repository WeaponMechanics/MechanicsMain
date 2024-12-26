package me.deecaad.core.file.simple

import com.cjcrafter.foliascheduler.util.ReflectionUtil
import me.deecaad.core.file.SimpleSerializer

class CsvSerializer<T : Any, S : SimpleSerializer<T>> : SimpleSerializer<List<T>> {
    private val serializer: S

    constructor(clazz: Class<S>) {
        serializer = ReflectionUtil.getConstructor(clazz).newInstance()
    }

    override fun getTypeName(): String {
        return "${serializer.typeName}1, ${serializer.typeName}2, ${serializer.typeName}3, ..."
    }

    override fun deserialize(
        data: String,
        errorLocation: String,
    ): List<T> {
        return data.split(", ?".toRegex()).map { serializer.deserialize(it, errorLocation) }
    }
}
