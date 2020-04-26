package me.deecaad.core.effects.shapes;

import org.bukkit.util.Vector;

/**
 * A shape is a set of points (or Vectors) relative
 * to an origin.
 */
public interface Shape extends Iterable<Vector> {

    /**
     * Should set the axis of the shape, where the
     * axis is defined as the vector that the shape
     * is "placed" around.
     *
     * @param vector The vector to draw around
     */
    void setAxis(Vector vector);

    /**
     * Should get whether or not the vector type for
     * this shape should be "guessed"
     * @see me.deecaad.core.utils.VectorUtils
     *
     * @return true if vector should be guessed
     */
    boolean isGuessVector();
}
