package me.deecaad.core.utils.primitive

/**
 * An entry in a mapping, which maps a generic key to a primitive integer.
 *
 * @param K The generic type of the key.
 * @property key The key used for hashing.
 * @property value The value stored in the mapping.
 */
abstract class IntEntry<K>(
    val key: K,
    var value: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntEntry<*>

        if (key != other.key) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key?.hashCode() ?: 0
        result = 31 * result + value
        return result
    }

    override fun toString(): String {
        return "$key: $value"
    }
}
