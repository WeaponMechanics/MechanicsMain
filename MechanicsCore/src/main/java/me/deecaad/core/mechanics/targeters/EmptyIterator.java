package me.deecaad.core.mechanics.targeters;

import java.util.Iterator;

public class EmptyIterator<E> implements Iterator<E> {

    private static final EmptyIterator<?> INSTANCE = new EmptyIterator<>();

    private EmptyIterator() {
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public E next() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("unchecked")
    public static <T> EmptyIterator<T> emptyIterator() {
        return (EmptyIterator<T>) INSTANCE;
    }
}
