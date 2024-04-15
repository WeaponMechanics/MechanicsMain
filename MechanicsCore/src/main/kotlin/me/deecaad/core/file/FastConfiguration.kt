package me.deecaad.core.file

import it.unimi.dsi.fastutil.objects.Object2BooleanMap
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2DoubleMap
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlin.jvm.Throws

/**
 * A fast implementation of the [Configuration] interface that uses primitive maps for storing values.
 */
class FastConfiguration : Configuration() {

    private val booleans: Object2BooleanMap<String> = Object2BooleanOpenHashMap()
    private val ints: Object2IntMap<String> = Object2IntOpenHashMap()
    private val doubles: Object2DoubleMap<String> = Object2DoubleOpenHashMap()
    private val objects: MutableMap<String, Any> = mutableMapOf()

    // Use to keep track of all key-value pairs, but not used for lookups
    private val all = mutableMapOf<String, Any>()

    override fun set(key: String, value: Any?) {
        if (value == null) {
            booleans.removeBoolean(key)
            ints.removeInt(key)
            doubles.removeDouble(key)
            objects.remove(key)
            all.remove(key)
            return
        }

        all[key] = value
        when (value) {
            is Boolean -> booleans.put(key, value)
            is Int -> ints.put(key, value)
            is Double -> doubles.put(key, value)
            else -> objects[key] = value
        }
    }

    @Throws(DuplicateKeyException::class)
    override fun copyFrom(other: Configuration) {
        val duplicates = mutableSetOf<String>()
        for ((key, value) in other) {
            if (key in all.keys)
                duplicates.add(key)
            else
                set(key, value)
        }

        if (duplicates.isNotEmpty())
            throw DuplicateKeyException(duplicates)
    }

    override fun clear() {
        booleans.clear()
        ints.clear()
        doubles.clear()
        objects.clear()

        all.clear()
    }

    override fun keys(): Set<String> {
        return all.keys
    }

    override fun entries(): Set<Map.Entry<String, Any>> {
        return all.entries
    }

    override fun values(): Collection<Any> {
        return all.values
    }

    override operator fun contains(key: String): Boolean {
        return key in all
    }

    override fun hasBoolean(key: String): Boolean {
        return key in booleans
    }

    override fun getBoolean0(key: String, def: Boolean): Boolean {
        booleans.defaultReturnValue(def)
        return booleans.getBoolean(key)
    }

    override fun hasInt(key: String): Boolean {
        return key in ints
    }

    override fun getInt0(key: String, def: Int): Int {
        ints.defaultReturnValue(def)
        return ints.getInt(key)
    }

    override fun hasDouble(key: String): Boolean {
        return key in doubles
    }

    override fun getDouble0(key: String, def: Double): Double {
        doubles.defaultReturnValue(def)
        return doubles.getDouble(key)
    }

    override fun hasObject(key: String): Boolean {
        return key in objects
    }

    override fun <T : Any> getObject0(key: String, def: T?, clazz: Class<T>): T? {
        return clazz.cast(objects.getOrDefault(key, def))
    }

    override fun iterator(): Iterator<Map.Entry<String, Any>> {
        return all.iterator()
    }
}
