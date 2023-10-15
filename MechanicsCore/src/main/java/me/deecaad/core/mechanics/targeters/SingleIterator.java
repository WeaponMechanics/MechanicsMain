package me.deecaad.core.mechanics.targeters;

import java.util.Iterator;

public class SingleIterator<E> implements Iterator<E> {

    private final E value;
    private boolean calledNext;

    public SingleIterator(E value) {
        this.value = value;
    }

    @Override
    public boolean hasNext() {
        return !calledNext;
    }

    @Override
    public E next() {
        if (calledNext)
            throw new IllegalStateException();

        calledNext = true;
        return value;
    }
}
