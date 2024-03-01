package me.deecaad.core.utils.primitive

import java.util.PrimitiveIterator
import java.util.Spliterator
import java.util.Spliterators

/**
 * This interface represents a collection of primitive integers. This has the
 * advantage of not needing to box and unbox integers when iterating over them.
 */
interface IntIterable {

    /**
     * Returns an iterator over the elements of this collection.
     */
    fun iterator(): PrimitiveIterator.OfInt

    /**
     * Returns a spliterator over the elements of this collection.
     */
    fun spliterator(): Spliterator.OfInt {
        return Spliterators.spliteratorUnknownSize(iterator(), 0)
    }
}

/**
 * Performs the given action for each element of the collection.
 *
 * @param action The action to be performed for each element.
 * @receiver The collection to iterate over.
 */
inline fun IntIterable.forEach(action: (Int) -> Unit) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        action(iterator.nextInt())
    }
}