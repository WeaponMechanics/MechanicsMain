package me.deecaad.core.effects.shapes;

import me.deecaad.core.mechanics.serialization.SerializerData;
import me.deecaad.core.mechanics.serialization.StringSerializable;
import org.bukkit.util.Vector;

/**
 * A shape is a set of points (or Vectors) relative
 * to an origin (0, 0, 0)
 */
@SerializerData(name = "shape", args = "vector~VECTOR~axis")
public abstract class Shape implements StringSerializable<Shape>, Iterable<Vector> {

    protected Vector axis;

    protected Shape() {
    }

    /**
     * Should set the axis of the shape, where the
     * axis is defined as the vector that the shape
     * is "placed" around.
     *
     * @param vector The vector to draw around
     */
    public void setAxis(Vector vector) {
        axis = vector;
    }

    /**
     * Gets the axis of the shape
     *
     * @return The current axis
     */
    public final Vector getAxis() {
        return axis.clone();
    }
}
