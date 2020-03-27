package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This explosion defines a rectangular prism shaped
 * explosion, where a valid (non-negative) width and
 * height are specified.
 */
public class CuboidExplosion implements Explosion {
    
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
    public Set<Block> getBlocks(@Nonnull Location origin) {
        Set<Block> temp = new HashSet<>();
        
        Location pos1 = origin.clone().add(-width, -height, -width);
        Location pos2 = origin.clone().add(+width, +height, +width);
    
        // Loops through a cuboid region between pos1 and pos2
        // effectively looping through every single block inside
        // of a square
        for (int x = pos1.getBlockX(); x < pos2.getBlockX(); x++) {
            for (int y = pos1.getBlockY(); y < pos2.getBlockY(); y++) {
                for (int z = pos1.getBlockZ(); z < pos2.getBlockZ(); z++) {
                    temp.add(new Location(origin.getWorld(), x, y, z).getBlock());
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
    public Set<LivingEntity> getEntities(@Nonnull Location origin) {
        double xMin = origin.getX() - width,  xMax = origin.getX() + width;
        double yMin = origin.getY() - height, yMax = origin.getY() + height;
        double zMin = origin.getZ() - width,  zMax = origin.getZ() + width;
        
        return origin.getWorld().getLivingEntities()
                .stream()
                .filter(entity -> {
                    double x = entity.getLocation().getX();
                    double y = entity.getLocation().getY();
                    double z = entity.getLocation().getZ();
                    
                    return  x >= xMin && x <= xMax &&
                            y >= yMin && y <= yMax &&
                            z >= zMin && z <= zMax;
                })
                .collect(Collectors.toSet());
                
    }
}
