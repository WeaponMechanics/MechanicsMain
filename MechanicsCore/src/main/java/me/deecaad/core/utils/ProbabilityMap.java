package me.deecaad.core.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class outlines a mapping of elements to a weight. This data structure
 * allows real time getting of random elements with weight.
 *
 * @param <E> The type of the element to store.
 */
public class ProbabilityMap<E> implements Iterable<ProbabilityMap.Node<E>> {

    private final Node<E> dummy;
    private final NavigableSet<Node<E>> set;
    private double totalProbability;

    /**
     * Default constructor.
     */
    public ProbabilityMap() {
        this.dummy = new Node<>();
        this.set = new TreeSet<>(Comparator.comparingDouble(Node::getOffset));
    }

    /**
     * Adds an <code>element</code> with the given weight to the map.
     *
     * @param element The element to add.
     * @param chance  The non-negative weight to map to the element.
     * @return <code>true</code> if the element was successfully added.
     */
    public boolean add(E element, double chance) {
        if (chance <= 0.0)
            throw new IllegalArgumentException("chance <= 0.0");

        Node<E> node = new Node<>(element, chance, totalProbability);
        if (set.add(node)) {
            totalProbability += chance;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the given element, if it is present in the map. This method
     * has an O notation of O(n) in both best and worst case scenarios.
     *
     * @param element The element to remove.
     * @return <code>true</code> if the element was removed.
     */
    public boolean remove(E element) {
        Node<E> removedElement = null;
        Iterator<Node<E>> iterator = iterator();

        // Iterate through every element
        while (iterator.hasNext()) {
            Node<E> node = iterator.next();

            // If the element has already been removed, then we
            // need to shift the rest of the elements back by the
            // removed element's chance
            if (removedElement != null) {
                node.offset -= removedElement.chance;
            } else if (Objects.equals(element, node.value)) {
                iterator.remove();
                totalProbability -= node.chance;
                removedElement = node;
            }
        }

        return removedElement != null;
    }

    /**
     * Returns a random element based on each element's weight. If there are no
     * elements in the set, then this method will return <code>null</code>.
     *
     * @return The randomized element.
     */
    @Nullable
    public E get() {
        dummy.offset = ThreadLocalRandom.current().nextDouble(totalProbability);
        Node<E> temp = set.floor(dummy);
        return temp == null ? null : temp.value;
    }

    /**
     * Returns <code>true</code> if there are no elements added to the map.
     *
     * @return <code>true</code> if the backing map is empty.
     */
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * Returns the number of elements in the map.
     *
     * @return The amount of elements in the map.
     */
    public int size() {
        return set.size();
    }

    @NotNull
    @Override
    public Iterator<Node<E>> iterator() {
        return set.iterator();
    }

    public static class Node<E> {

        private final E value;
        private final double chance;
        private double offset;

        Node() {
            value = null;
            chance = 0.0;
        }

        Node(E value, double chance, double offset) {
            this.chance = chance;
            this.value = value;
            this.offset = offset;
        }

        public E getValue() {
            return value;
        }

        public double getChance() {
            return chance;
        }

        public double getOffset() {
            return offset;
        }
    }
}
