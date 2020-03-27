package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Uses parabolas to calculate the area of explosions, where
 * a parabola is defined as:
 * y = m(x - xOffset)^2 + yOffset
 * xOffset is ignored here
 * yOffset is the depth. Negative values ONLY are allowed.
 * m is the angle. Under 1 makes the explosion wider. Over 1 makes the explosion thinner
 */
public class ParabolicExplosion implements Explosion {
    
    private double depth;   // This is assumed to be negative
    private double angle;
    
    public ParabolicExplosion(double depth) {
        this(depth, 0.5);
    }
    
    public ParabolicExplosion(double depth, double angle) {
        this.depth = depth;
        this.angle = angle;
    }
    
    /**
     * @return Depth of the explosion
     */
    public double getDepth() {
        return depth;
    }
    
    /**
     * @return Angle of the parabola
     */
    public double getAngle() {
        return angle;
    }
    
    @Nonnull
    @Override
    public Set<Block> getBlocks(@Nonnull Location origin) {
        Set<Block> temp = new HashSet<>();

        // Solve for x
        // y = angle * x^2 + depth
        // y - depth = angle * x^2
        // (y - depth) / angle = x^2
        // sqrt((y - depth) / angle) = x
        // Set y = 0 to find x intercept (sqrt always returns a positive double)
        // sqrt(-depth / angle) = x
        double intercept = Math.sqrt(-depth / angle);

        for (double x = -intercept; x < intercept; x++) {
            for (double y = depth; y < -depth; y++) {
                for (double z = -intercept; z < intercept; z++) {
                    if (test(x, y, z)) {
                        if (isNearEdge(x, y, z, 1.5) && Math.random() < 0.20) {
                            DebugUtil.log(LogLevel.DEBUG, "Skipping block (" + x + ", " + y + ", " + z + ") due to noise.");
                            continue; // outer noise checker
                        }

                        temp.add(origin.clone().add(x, y, z).getBlock());
                    }
                }
            }
        }
        return temp;
    }
    
    @Nonnull
    @Override
    public Set<LivingEntity> getEntities(@Nonnull Location origin) {
        return origin.getWorld().getEntities().stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> test(origin, entity.getLocation()))
                .map(entity -> (LivingEntity) entity)
                .collect(Collectors.toSet());
    }

    /**
     * Tests if a location is within this explosion
     * based on the given origin
     *
     * @param origin Center of the explosion
     * @param loc Point to test
     * @return If the location is in the explosion
     */
    public boolean test(Location origin, Location loc) {
        loc.subtract(origin);
        return test(loc.getX(), loc.getY(), loc.getZ());
    }

    public boolean test(double x, double y, double z) {
        double temp1 = +angle * (x * x) + depth - y;
        double temp2 = -angle * (x * x) - depth - y;
        double temp3 = +angle * (z * z) + depth - y;
        double temp4 = -angle * (z * z) - depth - y;

        return temp1 <= 0 && temp2 >= 0 && temp3 <= 0 && temp4 >= 0;
    }

    public boolean isNearEdge(double x, double y, double z, double distance) {
        double temp1 = +angle * (x * x) + depth - y;
        double temp2 = -angle * (x * x) - depth - y;
        double temp3 = +angle * (z * z) + depth - y;
        double temp4 = -angle * (z * z) - depth - y;

        return temp1 > -distance ||
                temp2 < distance ||
                temp3 > -distance ||
                temp4 < distance;
    }
}