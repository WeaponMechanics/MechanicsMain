package me.deecaad.core.utils.primitive

import java.util.*
import kotlin.collections.AbstractSet

/**
 * IntMap
 */
class IntMap<K : Any>(
    capacity: Int = 10,
    private val loadFactor: Float = 0.75f
): MutableIterable<IntEntry<K>> {

    private class Node<K>(
        key: K,
        value: Int,
        var hash: Int,
        var next: Node<K>? = null,
    ) : IntEntry<K>(key, value)

    private var table: Array<Node<K>?>? = null
    private var threshold: Int = capacity
    var size: Int = 0
        private set

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
            if (node!!.key == key)
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

    fun isEmpty(): Boolean {
        return size == 0
    }

    fun isNotEmpty(): Boolean {
        return size != 0
    }

    fun containsKey(key: Any): Boolean {
        return getNode(key) != null
    }

    fun containsValue(search: Int): Boolean {
        for ((_, value) in this) {
            if (value == search)
                return true
        }
        return false
    }

    operator fun get(key: Any): Int {
        val node = getNode(key)
        return node?.value ?: 0
    }

    operator fun set(key: K, value: Int): Int {
        if (table == null || size > threshold)
            resize()

        val hash = hash(key)
        val index = hash.and(table!!.size - 1)
        var node = table!![index]

        // Check to see if a node with a matching key already exists
        while (node != null) {
            if (node.hash == hash && node.key == key) {
                val oldValue = node.value
                node.value = value
                return oldValue
            }
            node = node.next
        }

        // No node with a matching key was found, so create a new one
        val newNode = Node(key, value, hash, table!![index])
        table!![index] = newNode
        size++
        return 0
    }

    fun remove(key: Any): Int {
        return removeNode(key)?.value ?: 0
    }

    private fun removeNode(key: Any): Node<K>? {
        val hash = hash(key)
        val index = hash.and(table!!.size - 1)
        var node = table?.get(index) ?: return null

        if (node.hash == hash && node.key == key) {
            table!![index] = node.next
            size--
            return node
        }

        var next = node.next
        while (next != null) {
            if (next.hash == hash && next.key == key) {
                node.next = next.next
                size--
                return next
            }
            node = next
            next = next.next
        }

        return null
    }

    fun clear() {
        val table = table ?: return
        for (i in table.indices) {
            table[i] = null
        }
        size = 0
    }

    inline fun forEach(action: (K, Int) -> Unit) {
        for ((key, value) in this) {
            action(key, value)
        }
    }

    private abstract inner class HashIterator {
        private var next: Node<K>? = null
        private var current: Node<K>? = null
        private var index: Int = 0

        init {
            // advance to first entry
            if (table != null && isNotEmpty()) {
                while (index < table!!.size && next == null) {
                    next = table!![index++]
                }
            }
        }

        fun hasNext(): Boolean {
            return next != null
        }

        fun nextNode(): Node<K> {
            val node = next ?: throw NoSuchElementException()
            current = node
            next = current!!.next

            // advance to next entry
            if (next == null && table != null) {
                while (index < table!!.size && next == null) {
                    next = table!![index++]
                }
            }
            return node
        }

        fun remove() {
            this@IntMap.remove(current!!.key)
        }
    }

    private inner class KeyIterator : HashIterator(), MutableIterator<K> {
        override fun next(): K {
            return nextNode().key
        }
    }

    private inner class ValueIterator : HashIterator(), PrimitiveIterator.OfInt {
        override fun nextInt(): Int {
            return nextNode().value
        }
    }

    private inner class EntryIterator : HashIterator(), MutableIterator<IntEntry<K>> {
        override fun next(): IntEntry<K> {
            return nextNode()
        }
    }

    private inner class KeySet : AbstractSet<K>() {
        override val size: Int = this@IntMap.size
        override fun iterator(): Iterator<K> = KeyIterator()
        override fun contains(element: K): Boolean = containsKey(element)
        override fun isEmpty(): Boolean = this@IntMap.isEmpty()
    }

    private inner class Values : IntCollection {
        override val size: Int = this@IntMap.size
        override fun iterator(): PrimitiveIterator.OfInt = ValueIterator()
        override fun contains(value: Int): Boolean = containsValue(value)
        override fun isEmpty(): Boolean = this@IntMap.isEmpty()
    }

    private inner class EntrySet : AbstractSet<IntEntry<K>>() {
        override val size: Int = this@IntMap.size
        override fun iterator(): Iterator<IntEntry<K>> = EntryIterator()

        override fun contains(element: IntEntry<K>): Boolean {
            val node = getNode(element.key) ?: return false
            return node.value == element.value
        }
    }

    override fun iterator(): MutableIterator<IntEntry<K>> {
        return EntryIterator()
    }

    /**
     * Returns a set view of the keys contained in this map.
     */
    val keys: Set<K> by lazy { KeySet() }

    /**
     * Returns a collection view of the values contained in this map.
     */
    val values: IntCollection by lazy { Values() }

    /**
     * Returns a set view of the entries contained in this map.
     */
    val entries: Set<IntEntry<K>> by lazy { EntrySet() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntMap<*>

        // Different sizes cannot be equal
        if (size != other.size)
            return false

        // Check each key-value pair. Looping through only 1 map is fine since
        // we already checked size^
        for ((key, value) in this) {
            if (value != other[key])
                return false
        }

        return true
    }

    override fun hashCode(): Int {
        var hash = 0
        for (entry in this) {
            hash += entry.hashCode()
        }
        return hash
    }

    companion object {
        const val DEFAULT_CAPACITY: Int = 16
        const val DEFAULT_LOAD_FACTOR: Float = 0.75f
        const val MAX_CAPACITY: Int = (1).shl(30)
    }
}