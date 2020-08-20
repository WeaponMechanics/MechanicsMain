package me.deecaad.core.utils;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SplittableRandom;
import java.util.TreeSet;

public class ProbabilityMap<E> implements Iterable<ProbabilityMap.Node<E>> {

    // Every instance of ProbabilityMap has access to the same
    // instance of Random
    private final SplittableRandom random = new SplittableRandom();

    private final Node<E> dummy;
    private NavigableSet<Node<E>> set;
    private double totalProbability;

    public ProbabilityMap() {
        this.dummy = new Node<>(null, 0.0, 0.0);
        this.set = new TreeSet<>(Comparator.comparingDouble(Node::getOffset));
        this.totalProbability = 0.0;
    }

    /**
     * Maps the given element <code>e</code> to it's
     * given probability
     *
     * @param e The element to add
     * @param chance The chance of selecting the
     * @return true if the element was added
     */
    public boolean add(E e, double chance) {
        Node<E> node = new Node<>(e, chance, totalProbability);
        if (set.add(node)) {
            totalProbability += chance;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the given element <code>e</code> from this
     * <code>ProbabilityMap</code>, if present.
     *
     * @param e The object to remove
     * @return true if the element was present
     */
    public boolean remove(@Nonnull E e) {
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
            } else if (e.equals(node.value)) {
                iterator.remove();
                totalProbability -= node.chance;
                removedElement = node;
            }
        }

        return removedElement != null;
    }

    /**
     * Gets a random element based on it's chance from the data structure
     *
     * @return Randomize element, or null if empty
     */
    public E get() {
        dummy.offset = random.nextDouble(totalProbability);
        Node<E> temp = set.floor(dummy);

        return temp == null ? null : temp.value;
    }

    /**
     * @return true if there are no elements in the set
     */
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * @return The number of elements in the set
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

        private E value;
        private double chance;
        private double offset;

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
