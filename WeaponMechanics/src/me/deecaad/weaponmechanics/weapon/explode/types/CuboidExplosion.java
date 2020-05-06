package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionShape;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * This explosion defines a rectangular prism shaped
 * explosion, where a valid (non-negative) width and
 * height are specified.
 */
public class CuboidExplosion implements ExplosionShape {

    private static final Configuration config = WeaponMechanics.getBasicConfigurations();

    // These are set to be half the actual
    // values, kind of like radius
    private double width;
    private double height;
    
    /**
     * Constructs a <code>CuboidExplosion</code> object. The
     * width and height are divided by 2 to get the "radius"
     * of the rectangle.
     *
     * This is used when finding points about the origin,
     * the width input is the total width of the explosion.
     * The width instance variable is the distance from one
     * point to the origin.
     *
     * @param width  Total width of the explosion
     * @param height Total height of the explosion
     */
    public CuboidExplosion(double width, double height) {
        this.width = width / 2.0;
        this.height = height / 2.0;
    }
    
    /**
     * Gets all blocks between 2 points, where the 2
     * points are found using this width, and height
     * as well as the method's origin parameter.
     *
     * @param origin The center of the explosion
     *               Should not be null
     * @return The blocks within the explosion
     */
    @Nonnull
    @Override
    public List<Block> getBlocks(@Nonnull Location origin) {
        List<Block> temp = new ArrayList<>();

        double noiseDistance = config.getDouble("Explosions.Spherical.Noise_Distance", 1.25);
        double noiseChance = config.getDouble("Explosions.Spherical.Noise_Chance", 0.25);

        World world = origin.getWorld();
        if (world == null) {
            debug.log(LogLevel.WARN, "Cuboid explosion's origin was null? Origin:", origin.toString());
            return temp;
        }
        int blockX = origin.getBlockX();
        int blockY = origin.getBlockY();
        int blockZ = origin.getBlockZ();

        for (int x = (int) -width; x < width; x++) {
            for (int y = (int) -height; y < height; y++) {
                for (int z = (int) -width; z < width; z++) {

                    // Noise checker
                    if (Math.random() < noiseChance && isNearEdge(x, y, z, noiseDistance)) {
                        debug.log(LogLevel.DEBUG, "Skipping block(" + x + ", " + y + ", " + z + ") due to noise.");
                        continue; // outer noise checker
                    }

                    temp.add(world.getBlockAt(x + blockX, y + blockY, z + blockZ));
                }
            }
        }
        return temp;
    }
    
    /**
     * Gets any <code>LivingEntity</code>s inside this
     * <code>CuboidExplosion</code> by getting the entity's
     * (x, y, z) coordinates, and checking if that point
     * is in between the 2 points. This is done via
     *
     * <blockquote><pre>{@code
     *     point1.x <= entity.x <= point2.x
     *     point1.y <= entity.y <= point2.y
     *     point1.z <= entity.z <= point2.z
     * }</pre></blockquote>
     *
     * @param origin The center of the explosion
     *               Should not be null
     * @return All entities within the explosion
     */
    @Nonnull
    @Override
    public Map<LivingEntity, Double> getEntities(@Nonnull Location origin) {
        double xMin = origin.getX() - width,  xMax = origin.getX() + width;
        double yMin = origin.getY() - height, yMax = origin.getY() + height;
        double zMin = origin.getZ() - width,  zMax = origin.getZ() + width;
        
        List<LivingEntity> entities = origin.getWorld().getLivingEntities()
                .stream()
                .filter(entity -> {
                    double x = entity.getLocation().getX();
                    double y = entity.getLocation().getY();
                    double z = entity.getLocation().getZ();
                    
                    return  x >= xMin && x <= xMax &&
                            y >= yMin && y <= yMax &&
                            z >= zMin && z <= zMax;
                })
                .collect(Collectors.toList());

        if (entities.isEmpty()) return new HashMap<>();

        // This is imprecise, but super lightweight compared to using
        // trig to find the proper max distance for every entity
        double maxDistance = Math.sqrt(width * width + height * height);

        Map<LivingEntity, Double> temp = new HashMap<>(entities.size());
        for (LivingEntity entity : entities) {
            double distance = origin.distance(entity.getLocation());
            temp.put(entity, distance / maxDistance);
        }
        return temp;
    }

    public boolean isNearEdge(double x, double y, double z, double distance) {
        x = width - Math.abs(x);
        y = height - Math.abs(y);
        z = width - Math.abs(z);

        return  x < distance ||
                y < distance ||
                z < distance;
    }
}
