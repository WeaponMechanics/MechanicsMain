package me.deecaad.core.utils.primitive

import java.util.*
import java.util.stream.IntStream
import java.util.stream.StreamSupport

/**
 * This interface represents a collection of primitive integers. This has the
 * advantage of not needing to box and unbox integers when iterating over them.
 */
interface IntCollection : IntIterable {

    /**
     * Returns the number of elements in this collection.
     */
    val size: Int

    /**
     * Returns true if the collection contains the specified element. Returns
     * false otherwise.
     *
     * @param value The value to check for.
     * @return True if the collection contains the specified element.
     */
    fun contains(value: Int): Boolean

    /**
     * Returns true if the collection is empty. Returns false otherwise.
     *
     * @return True if the collection is empty.
     */
    fun isEmpty(): Boolean

    override fun spliterator(): Spliterator.OfInt {
        return Spliterators.spliterator(iterator(), size.toLong(), 0)
    }

    fun stream(): IntStream {
        return StreamSupport.intStream(spliterator(), false)
    }
}