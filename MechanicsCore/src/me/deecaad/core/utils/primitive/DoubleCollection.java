package me.deecaad.core.utils.primitive;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

public interface DoubleCollection extends DoubleIterable {

    int size();

    boolean isEmpty();

    boolean contains(double num);

    boolean add(double num);

    boolean remove(double num);

    void clear();

    @Override
    default Spliterator.OfDouble spliterator() {
        return Spliterators.spliterator(iterator(), size(), 0);
    }

    default DoubleStream stream() {
        return StreamSupport.doubleStream(spliterator(), false);
    }
}
