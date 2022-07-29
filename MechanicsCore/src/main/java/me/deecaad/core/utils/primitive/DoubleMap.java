package me.deecaad.core.utils.primitive;

import me.deecaad.core.utils.NumberUtil;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.function.Consumer;

public class DoubleMap<K> {

    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static class Node<K> extends DoubleEntry<K> {

        K key;
        double value;
        int hash;

        Node<K> next;

        public Node(K key, double value, int hash) {
            this.key = key;
            this.value = value;
            this.hash = hash;
        }

        @Override public K getKey() { return key; }
        @Override public double getValue() { return value; }

        @Override
        public double setValue(double value) {
            double old = this.value;
            this.value = value;
            return old;
        }
    }

    private Node<K>[] table;
    private final float loadFactor;
    private int threshold;
    private int size;

    private Set<DoubleEntry<K>> entrySet;
    private Set<K> keySet;
    private DoubleCollection values;

    public DoubleMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    public DoubleMap(int cap) {
        this();

        this.threshold = cap;
    }

    // Internal mapping methods

    private int hash(Object key) {
        return key.hashCode();
    }

    private Node<K> getNode(Object key) {
        if (table == null)
            return null;

        Node<K> node = table[hash(key) & (table.length - 1)];

        if (node != null) {
            do {
                if (Objects.equals(node.key, key))
                    return node;
            } while ((node = node.next) != null);
        }

        return null;
    }

    private void resize() {
        Node<K>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                    (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings("unchecked")
        Node<K>[] newTab = (Node<K>[]) new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K> node;
                if ((node = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (node.next == null)
                        newTab[node.hash & (newCap - 1)] = node;
                    else {
                        Node<K> loHead = null, loTail = null;
                        Node<K> hiHead = null, hiTail = null;
                        Node<K> next;
                        do {
                            next = node.next;
                            if ((node.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = node;
                                else
                                    loTail.next = node;
                                loTail = node;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = node;
                                else
                                    hiTail.next = node;
                                hiTail = node;
                            }
                        } while ((node = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
    }

    // Query Operations

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(Object key) {
        return getNode(key) != null;
    }

    public boolean containsValue(double value) {
        ValueIterator iterator = new ValueIterator();
        while (iterator.hasNext()) {
            if (NumberUtil.equals(value, iterator.nextDouble())) {
                return true;
            }
        }
        return false;
    }

    public double get(Object key) {
        Node<K> node = getNode(key);
        return node == null ? 0.0 : node.value;
    }

    // Modification Operations

    public double put(K key, double value) {
        if (table == null)
            resize();

        int hash = hash(key);
        int index = hash & (table.length - 1);
        Node<K> node = table[index];
        if (node == null)
            table[index] = new Node<>(key, value, hash);
        else {
            Node<K> temp;
            if (node.hash == hash && Objects.equals(key, node.hash))
                temp = node;
            else {
                while (true) {
                    if ((temp = node.next) == null) {
                        node.next = new Node<>(key, value, hash);
                        break;
                    }
                    if (temp.hash == hash && Objects.equals(key, temp.key))
                        break;

                    node = temp;
                }
            }
            if (temp != null) {
                double old = temp.value;
                temp.value = value;
                return old;
            }
        }

        if (++size > threshold)
            resize();
        return 0.0;
    }

    public double remove(Object key) {
        Node<K> node = removeNode(key);
        return node == null ? 0.0 : node.getValue();
    }

    private Node<K> removeNode(Object key) {
        int hash = hash(key);
        int index = hash & table.length - 1;
        Node<K> node = table[index];

        if (hash == node.hash && Objects.equals(key, node.key)) {
            table[index] = node.next;
            return node;
        } else {
            Node<K> temp;
            while (true) {
                if ((temp = node.next) == null)
                    return null;

                if (hash == temp.hash && Objects.equals(key, temp.key))
                    return temp;

                node = temp;
            }
        }
    }

    public void clear() {
        if (table != null && size > 0) {
            size = 0;
            Arrays.fill(table, null);
        }
    }

    // Functional Interface Operations

    public void forEach(BiDoubleConsumer<K> consumer) {
        EntryIterator iterator = new EntryIterator();
        while (iterator.hasNext()) {
            DoubleEntry<K> entry = iterator.next();
            consumer.accept(entry.getKey(), entry.getValue());
        }
    }

    // Viewing Operations (Different Views)

    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }

    public DoubleCollection values() {
        if (values == null) {
            values = new Values();
        }
        return values;
    }

    public Set<DoubleEntry<K>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    private class KeySet extends AbstractSet<K> {
        public int size()                 { return size; }
        public void clear()               { DoubleMap.this.clear(); }
        public Iterator<K> iterator()     { return new KeyIterator(); }
        public boolean contains(Object o) { return containsKey(o); }
        public boolean remove(Object o)   { return DoubleMap.this.removeNode(o) != null; }
    }

    private class Values implements DoubleCollection {

        public int size()                            { return size; }
        public boolean isEmpty()                     { return DoubleMap.this.isEmpty(); }
        public boolean contains(double num)          { return containsValue(num); }
        public boolean add(double num)               { throw new UnsupportedOperationException(); }
        public void clear()                          { DoubleMap.this.clear(); }
        public PrimitiveIterator.OfDouble iterator() { return new ValueIterator(); }

        public boolean remove(double num) {
            PrimitiveIterator.OfDouble iterator = iterator();
            while (iterator.hasNext()) {
                if (NumberUtil.equals(num, iterator.nextDouble())) {
                    iterator.remove();
                    return true;
                }
            }
            return false;
        }
    }

    private class EntrySet extends AbstractSet<DoubleEntry<K>> {

        public int size()                          { return size; }
        public void clear()                        { DoubleMap.this.clear(); }
        public Iterator<DoubleEntry<K>> iterator() { return new EntryIterator(); }

        public boolean contains(Object other) {
            if (!(other instanceof DoubleEntry))
                return false;
            DoubleEntry<?> entry = (DoubleEntry<?>) other;
            Node<K> candidate = getNode(entry.getKey());
            return candidate != null && candidate.equals(entry);
        }

        public boolean remove(Object other) {
            if (!(other instanceof DoubleEntry))
                return false;
            DoubleEntry<?> entry = (DoubleEntry<?>) other;
            return remove(entry.getKey());
        }

        public void forEach(Consumer<? super DoubleEntry<K>> action) {
            EntryIterator iterator = new EntryIterator();
            while (iterator.hasNext()) {
                DoubleEntry<K> entry = iterator.next;
                action.accept(entry);
            }
        }
    }

    // Iterators

    private abstract class HashIterator {
        Node<K> next;
        Node<K> current;
        int index;

        HashIterator() {
            current = next = null;
            index = 0;
            if (table != null && size > 0) { // advance to first entry
                do {} while (index < table.length && (next = table[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K> nextNode() {
            Node<K> node = next;
            if (node == null)
                throw new NoSuchElementException();
            if ((next = (current = node).next) == null && table != null) {
                do {} while (index < table.length && (next = table[index++]) == null);
            }
            return node;
        }

        public final void remove() {
            DoubleMap.this.remove(current.getKey());
            current = null;
        }
    }

    final class KeyIterator extends HashIterator
            implements Iterator<K> {
        public final K next() { return nextNode().getKey(); }
    }

    final class ValueIterator extends HashIterator
            implements PrimitiveIterator.OfDouble {

        public double nextDouble() { return nextNode().getValue(); }
        public final Double next() { return nextNode().getValue(); }
    }

    final class EntryIterator extends HashIterator
            implements Iterator<DoubleEntry<K>> {

        public final DoubleEntry<K> next() {
            return new DoubleEntry<K>() {
                private final Node<K> node = nextNode();
                @Override public K getKey() { return node.getKey(); }
                @Override public double getValue() { return node.getValue(); }
                @Override public double setValue(double value) { return node.setValue(value); }
            };
        }
    }

    // Object Operations

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof DoubleMap))
            return false;
        DoubleMap<?> map = (DoubleMap<?>) other;
        if (size() != map.size())
            return false;

        EntryIterator iterator = new EntryIterator();
        while (iterator.hasNext()) {
            DoubleEntry<K> entry = iterator.next();
            if (!entry.equals(map.getNode(entry.getKey())))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        EntryIterator iterator = new EntryIterator();
        while (iterator.hasNext())
            hash += iterator.next().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        EntryIterator iterator = new EntryIterator();
        if (!iterator.hasNext())
            return "{}";

        StringBuilder builder = new StringBuilder("{");
        while (true) {
            DoubleEntry<K> entry = iterator.next();
            K key = entry.getKey();
            double value = entry.getValue();

            // Check for recursion
            builder.append(key == this ? "(this Map)" : key);
            builder.append('=');
            builder.append(value);

            if (!iterator.hasNext())
                return builder.append('}').toString();
            builder.append(',').append(' ');
        }
    }
}
