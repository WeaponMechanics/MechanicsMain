package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionShape;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * Uses parabolas to calculate the area of explosions, where
 * a parabola is defined as:
 * y = m(x - xOffset)^2 + yOffset
 * xOffset is ignored here
 * yOffset is the depth. Negative values ONLY are allowed.
 * m is the angle. Under 1 makes the explosion wider. Over 1 makes the explosion thinner
 */
public class ParabolicExplosion implements ExplosionShape {

    private static final Configuration config = WeaponMechanics.getBasicConfigurations();

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
    public List<Block> getBlocks(@Nonnull Location origin) {
        List<Block> temp = new ArrayList<>();

        // Solve for x
        // y = angle * x^2 + depth
        // y - depth = angle * x^2
        // (y - depth) / angle = x^2
        // sqrt((y - depth) / angle) = x
        // Set y = 0 to find x intercept (sqrt always returns a positive double)
        // sqrt(-depth / angle) = x
        double intercept = Math.sqrt(-depth / angle);

        double noiseDistance = config.getDouble("Explosions.Spherical.Noise_Distance", 1.25);
        double noiseChance = config.getDouble("Explosions.Spherical.Noise_Chance", 0.25);

        for (double x = -intercept; x < intercept; x++) {
            for (double y = depth; y < -depth; y++) {
                for (double z = -intercept; z < intercept; z++) {
                    if (test(x, y, z)) {

                        // Checking chance first for resource usage
                        if (Math.random() < noiseChance && isNearEdge(x, y, z, noiseDistance)) {
                            debug.log(LogLevel.DEBUG, "Skipping block (" + x + ", " + y + ", " + z + ") due to noise.");
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
    public Map<LivingEntity, Double> getEntities(@Nonnull Location origin) {
        List<LivingEntity> entities = origin.getWorld().getEntities().stream()
                .filter(LivingEntity.class::isInstance)
                .filter(entity -> test(origin, entity.getLocation()))
                .map(LivingEntity.class::cast)
                .collect(Collectors.toList());

        if (entities.isEmpty()) return new HashMap<>();

        Map<LivingEntity, Double> temp = new HashMap<>(entities.size());
        World world = origin.getWorld();


        for (LivingEntity entity : entities) {
            Vector vector = origin.toVector().subtract(entity.getLocation().toVector());

            int traces = 0;
            int successfulTraces = 0;

            for (int i = 0; i < 8; i++) {
                double x = NumberUtils.random(-.75, .75);
                double y = NumberUtils.random(-.75, .75);
                double z = NumberUtils.random(-.75, .75);
                Vector noise = new Vector(x, y, z);
                vector.add(noise);
                RayTraceResult trace = world.rayTraceBlocks(origin, vector, vector.length(), FluidCollisionMode.NEVER);
                vector.subtract(noise);

                // If the trace found no blocks
                if (trace.getHitBlock() == null) {
                    successfulTraces++;
                }

                traces++;
            }

            temp.put(entity, ((double) successfulTraces) / traces);
        }

        return temp;
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

    @Override
    public String toString() {
        return "ParabolicExplosion{" +
                "depth=" + depth +
                ", angle=" + angle +
                '}';
    }
}