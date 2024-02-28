package me.deecaad.core.utils.primitive

import java.util.*

/**
 * IntMap
 */
class IntMap<K>(capacity: Int = 10, private val loadFactor: Float = 0.75f) {

    private class Node<K>(
        key: K,
        value: Int,
        var hash: Int,
        var next: Node<K>? = null,
    ) : IntEntry<K>(key, value)

    private var table: Array<Node<K>?>? = null
    private var threshold: Int = capacity
    private var size: Int = 0

    private fun hash(key: Any): Int {
        // We can use this to adjust hashing methods as needed. For example,
        // java.util.HashMap xors the lower 16 bits with the higher 16 bits
        // and vice versa, assuming that some hashCode implementations are poor.
        return key.hashCode()
    }

    private fun getNode(key: Any): Node<K>? {
        val table = table ?: return null

        val hash = hash(key)
        var node: Node<K>? = table[hash.and(table.size - 1)] ?: return null

        do {
            if (Objects.equals(node!!.key, key))
                return node

            node = node.next
        } while (node != null)


        return null
    }

    private fun resize() {
        val oldTable = table
        val oldCapacity = oldTable?.size ?: 0
        val oldThreshold = threshold
        val newCapacity: Int
        var newThreshold = 0

        if (oldCapacity > 0) {
            newCapacity = oldCapacity.shl(1)

            // Map cannot be resized any bigger
            if (oldCapacity >= MAX_CAPACITY) {
                threshold = Integer.MAX_VALUE
                return
            } else if (newCapacity < MAX_CAPACITY && oldCapacity >= DEFAULT_CAPACITY) {
                newThreshold = oldThreshold.shl(1)
            }
        } else if (oldThreshold > 0) {
            newCapacity = oldThreshold
        } else {
            newCapacity = DEFAULT_CAPACITY
            newThreshold = (DEFAULT_LOAD_FACTOR * DEFAULT_CAPACITY).toInt()
        }
        this.threshold = newThreshold
        val newTable = arrayOfNulls<Node<K>>(newCapacity)
        this.table = newTable

        // No need to rehash
        if (oldTable == null)
            return

        for (i in 0 until oldCapacity) {
            var current: Node<K>? = oldTable[i] ?: continue
            oldTable[i] = null // GC

            // Single node, no bucket
            if (current!!.next == null) {
                newTable[current.hash.and(newCapacity - 1)] = current
                continue
            }

            var loHead: Node<K>? = null
            var loTail: Node<K>? = null
            var hiHead: Node<K>? = null
            var hiTail: Node<K>? = null
            var next: Node<K>?

            do {
                next = current!!.next

                if (current.hash.and(oldCapacity) == 0) {
                    if (loTail == null)
                        loHead = current
                    else
                        loTail.next = current
                    loTail = current
                } else {
                    if (hiTail == null)
                        hiHead = current
                    else
                        hiTail.next = current
                    hiTail = current
                }

                current = next
            } while (current != null)

            if (loTail != null) {
                loTail.next = null
                newTable[i] = loHead
            }
            if (hiTail != null) {
                hiTail.next = null
                newTable[i] = hiHead
            }
        }
    }


    companion object {
        const val DEFAULT_CAPACITY: Int = 16
        const val DEFAULT_LOAD_FACTOR: Float = 0.75f
        const val MAX_CAPACITY: Int = (1).shl(30)
    }
}