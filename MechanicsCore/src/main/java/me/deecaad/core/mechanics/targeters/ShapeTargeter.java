package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.CastData;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Iterator;

public abstract class ShapeTargeter extends RelativeTargeter {

    /**
     * Default constructor for serializer
     */
    public ShapeTargeter() {
    }

    @Override
    public boolean isEntity() {
        return false;
    }

    @Override
    protected final Iterator<CastData> getTargets0(CastData cast) {
        Iterator<Vector> points = getPoints();
        Location source = isUseTarget ? cast.getTargetLocation() : cast.getSourceLocation();

        // We may have been targeting an entity or a got a cloned location. We
        // must set the target location here, so we are modifying the reference.
        cast.setTargetLocation(source);

        return new Iterator<>() {
            Vector previous;

            @Override
            public boolean hasNext() {
                return points.hasNext();
            }

            @Override
            public CastData next() {
                // Subtract the previous point, if present
                if (previous != null)
                    source.subtract(previous);

                previous = points.next();
                source.add(previous);
                return cast;
            }
        };
    }

    public abstract Iterator<Vector> getPoints();
}
