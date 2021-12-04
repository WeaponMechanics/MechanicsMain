package me.deecaad.core.utils.primitive;

/**
 * This interface outlines some action that accepts a generic first argument
 * and a double second argument. This interface has the advantage of not
 * needing to wrap and unwrap a {@link Double}.
 *
 * @param <T> The first argument generic type.
 * @see DoubleMap#forEach(BiDoubleConsumer)
 */
@FunctionalInterface
public interface BiDoubleConsumer<T> {
    void accept(T t, double num);
}
