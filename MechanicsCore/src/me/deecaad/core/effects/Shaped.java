package me.deecaad.core.effects;

import me.deecaad.core.effects.shapes.Shape;
import org.bukkit.util.Vector;

public interface Shaped {

    void setAxis(Vector vector);

    Shape getShape();

    void setShape(Shape shape);
}