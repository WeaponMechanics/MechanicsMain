package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.utils.NumberUtil;
import org.bukkit.Location;
import org.bukkit.World;
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
    private boolean isTraceDown;

    /**
     * Default constructor for serializer
     */
    public ScatterTargeter() {
    }

    public ScatterTargeter(int points, double horizontalRange, double verticalRange, boolean isTraceDown) {
        this.points = points;
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
        this.isTraceDown = isTraceDown;
    }

    @Override
    public @NotNull Iterator<Vector> getPoints(@NotNull CastData cast) {

        // Get world for traceDown
        Location origin = cast.getTargetLocation();
        World world = cast.getTargetWorld();
        if (origin == null || world == null) {
            MechanicsCore.debug.error("Tried to use useTarget=true with Scatter{}, but there was no target");
            return EmptyIterator.emptyIterator();
        }

        // Max attempts to try traceDown
        final int maxAttempts = 5;

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

                // Reset until we don't spawn in a block
                int attempts = 0;
                double x, y, z;
                do {
                    x = NumberUtil.random(-horizontalRange, +horizontalRange);
                    y = NumberUtil.random(-verticalRange, +verticalRange);
                    z = NumberUtil.random(-horizontalRange, +horizontalRange);
                } while (isTraceDown && attempts++ < maxAttempts && !isEmpty(world, origin, x, y, z));

                // Trace down to either the ground or the bottom of the vertical range.
                int dy = 0;
                while (isTraceDown && dy++ < verticalRange && isEmpty(world, origin, x, y, z)) {
                    y--;
                    dy++;
                }

                // If we are in a solid block, we have succeeded in finding ground!
                // Now we should go to the top of the block.
                if (isTraceDown)
                    y = Math.floor(y + 1) + 0.05;

                cache.setX(x);
                cache.setY(y);
                cache.setZ(z);
                return cache;
            }
        };
    }

    private static boolean isEmpty(World world, Location origin, double x, double y, double z) {
        return world.getBlockAt((int) (origin.getX() + x), (int) (origin.getY() + y), (int) (origin.getZ() + z)).isEmpty();
    }

    @Override
    public String getKeyword() {
        return "Scatter";
    }

    @NotNull
    @Override
    public Targeter serialize(@NotNull SerializeData data) throws SerializerException {
        int points = data.of("Points").assertExists().getInt();
        double horizontalRange = data.of("Horizontal_Range").getDouble(5.0);
        double verticalRange = data.of("Vertical_Range").getDouble(horizontalRange);
        boolean isTraceDown = data.of("Trace_Down").getBool(false);
        return applyParentArgs(data, new ScatterTargeter(points, horizontalRange / 2.0, verticalRange / 2.0, isTraceDown));
    }
}
