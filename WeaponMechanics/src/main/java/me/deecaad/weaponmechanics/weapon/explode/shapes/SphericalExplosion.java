package me.deecaad.weaponmechanics.weapon.explode.shapes;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class SphericalExplosion implements ExplosionShape {

    private static final Configuration config = WeaponMechanics.getBasicConfigurations();

    private final double radius;
    private final double radiusSquared;
    
    public SphericalExplosion(double radius) {
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }
    
    @NotNull
    @Override
    public List<Block> getBlocks(@NotNull Location origin) {
        List<Block> temp = new ArrayList<>();
        
        Location pos1 = origin.clone().add(-radius, -radius, -radius);
        Location pos2 = origin.clone().add(+radius, +radius, +radius);

        double noiseDistance = NumberConversions.square(config.getDouble("Explosions.Spherical.Noise_Distance", 1.0));
        double noiseChance = config.getDouble("Explosions.Spherical.Noise_Chance", 0.10);

        // Loops through a cuboid region between pos1 and pos2
        // effectively looping through every single block inside
        // of a square
        for (int x = pos1.getBlockX(); x < pos2.getBlockX(); x++) {
            for (int y = pos1.getBlockY(); y < pos2.getBlockY(); y++) {
                for (int z = pos1.getBlockZ(); z < pos2.getBlockZ(); z++) {
                    Location loc = new Location(origin.getWorld(), x, y, z);
                    
                    // If the distance between the current iteration
                    // and the origin is less than the radius of the
                    // sphere. This "reshapes" the cube into a sphere
                    double distance = loc.distanceSquared(origin);
                    if (distance <= radiusSquared) {

                        boolean isNearEdge = radiusSquared - distance < noiseDistance;
                        if (isNearEdge && NumberUtil.chance(noiseChance)) {
                            debug.log(LogLevel.DEBUG, "Skipping block (" + x + ", " + y + ", " + z + ") due to noise.");
                            continue; // outer noise checker
                        }

                        temp.add(loc.getBlock());
                    }
                }
            }
        }
        return temp;
    }
    
    @NotNull
    @Override
    public List<LivingEntity> getEntities(@NotNull Location origin) {
        return origin.getWorld().getLivingEntities()
                .stream()
                .filter(entity -> entity.getLocation().distanceSquared(origin) < radiusSquared)
                .collect(Collectors.toList());
    }

    @Override
    public double getMaxDistance() {
        return radius;
    }

    @Override
    public boolean isContained(@NotNull Location origin, @NotNull Location point) {
        return origin.distanceSquared(point) < radiusSquared;
    }

    @Override
    public double getArea() {
        return 4.0 / 3.0 * Math.PI * radius * radius * radius;
    }

    @Override
    public String toString() {
        return "SphericalExplosion{" +
                "radius=" + radius +
                '}';
    }
}
