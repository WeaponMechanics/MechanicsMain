package me.deecaad.core.utils.primitive;

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoubleConsumer;

/**
 * This interface outlines a collection that can be iterated over. This
 * interface has the advantage of not needing to box and unbox a {@link Double}
 * wrapper.
 *
 * @see Iterable
 */
public interface DoubleIterable {

    PrimitiveIterator.OfDouble iterator();

    default void forEach(DoubleConsumer consumer) {
        PrimitiveIterator.OfDouble iterator = iterator();

        while (iterator.hasNext())
            consumer.accept(iterator.nextDouble());
    }

    default Spliterator.OfDouble spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }
}