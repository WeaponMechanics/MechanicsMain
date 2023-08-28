package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.NumberUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * Strictly speaking, a random assortment of points is not a shape. But the
 * shape targeter targets a list of points, not a shape, so this is fine.
 */
public class ScatterTargeter extends ShapeTargeter {

    private int points;
    private double horizontalRange;
    private double verticalRange;

    /**
     * Default constructor for serializer
     */
    public ScatterTargeter() {
    }

    public ScatterTargeter(int points, double horizontalRange, double verticalRange) {
        this.points = points;
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
    }

    @Override
    public Iterator<Vector> getPoints() {
        return new Iterator<>() {
            final Vector cache = new Vector();
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < points;
            }

            @Override
            public Vector next() {
                i++;
                double x = NumberUtil.random(-horizontalRange, +horizontalRange);
                double y = NumberUtil.random(-verticalRange, +verticalRange);
                double z = NumberUtil.random(-horizontalRange, +horizontalRange);
                cache.setX(x);
                cache.setY(y);
                cache.setZ(z);
                return cache;
            }
        };
    }

    @Override
    public String getKeyword() {
        return "Scatter";
    }

    @NotNull
    @Override
    public Targeter serialize(SerializeData data) throws SerializerException {
        int points = data.of("Points").assertExists().getInt();
        double horizontalRange = data.of("Horizontal_Range").getDouble(5.0);
        double verticalRange = data.of("Vertical_Range").getDouble(0.0);
        return applyParentArgs(data, new ScatterTargeter(points, horizontalRange, verticalRange));
    }
}
