package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.projectile.HitBox;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.utils.MaterialHelper;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionShape;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
/**
 * This explosion uses the minecraft explosion system,
 * instead of using a shape.
 *
 * https://minecraft.gamepedia.com/Explosion
 */
public class DefaultExplosion implements ExplosionShape {

    private static final int GRID_SIZE = 8; // 16 minecraft default
    private static final int BOUND = GRID_SIZE - 1;
    private static final double DECAY_RATE = 0.3;
    private static final double ABSORB_RATE = 0.3;

    private float yield;
    
    public DefaultExplosion(double yield) {
        this.yield = (float) yield;
    }
    
    @Nonnull
    @Override
    public List<Block> getBlocks(@Nonnull Location origin) {
        if (yield >= 0.1F) {
            World world = origin.getWorld();

            List<Block> set = new ArrayList<>();

            // Separates the explosion into a 16 by 16 by 16
            // grid.
            for (int k = 0; k < GRID_SIZE; ++k) {
                for (int i = 0; i < GRID_SIZE; ++i) {
                    for (int j = 0; j < GRID_SIZE; ++j) {

                        // Checking if the the point defined by (k, i, j) is on the grid
                        if (k == 0 || k == BOUND || i == 0 || i == BOUND || j == 0 || j == BOUND) {

                            // d representing change, so change in x, change in y, etc
                            double dx = ((double) k) / BOUND * 2 - 1;
                            double dy = ((double) i) / BOUND * 2 - 1;
                            double dz = ((double) j) / BOUND * 2 - 1;
                            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

                            // normalize
                            dx /= length;
                            dy /= length;
                            dz /= length;

                            double x = origin.getX();
                            double y = origin.getY();
                            double z = origin.getZ();

                            // Slightly randomized intensity, based on the yield of the explosion
                            float intensity = yield * (0.7F + NumberUtils.random().nextFloat() * 0.6F);

                            while(intensity > 0.0f) {
                                Block block = world.getBlockAt((int) x, (int) y, (int) z);
                                Material type = block.getType();
                                boolean isAir = MaterialHelper.isAir(type);

                                if (!isAir) {
                                    float resistance = MaterialHelper.getBlastResistance(type);

                                    intensity -= (resistance + 0.3F) * ABSORB_RATE;
                                }

                                if (intensity > 0.0F && y < 256 && y >= 0) {
                                    set.add(block);
                                }

                                x += dx * DECAY_RATE;
                                y += dy * DECAY_RATE;
                                z += dz * DECAY_RATE;

                                // Ray decays over longer distance
                                intensity -= DECAY_RATE * 0.75;
                            }
                        }
                    }
                }
            }
            return set;
        }
        else return new ArrayList<>();
    }
    
    @Nonnull
    @Override
    public Map<LivingEntity, Double> getEntities(@Nonnull Location origin) {

        // Map to store all the calculated entities in
        Map<LivingEntity, Double> temp = new HashMap<>();

        // How far away from the explosion to damage players
        double damageRadius = yield * 2.0F;
        double damageRadiusOuter = damageRadius + 1;

        // Gets data on the location of the explosion
        World world = origin.getWorld();
        double x = origin.getX();
        double y = origin.getY();
        double z = origin.getZ();

        if (world == null) {
            debug.log(LogLevel.ERROR, "Explosion in null world? Location: " + origin, "Please report error to devs");
            return temp;
        }

        // Get all entities within the damageable radius
        // Only can damage LivingEntities
        Collection<LivingEntity> entities = world
                .getNearbyEntities(origin, damageRadiusOuter, damageRadiusOuter, damageRadiusOuter)
                .stream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .collect(Collectors.toList());

        Vector vector = new Vector(x, y, z);

        for (LivingEntity entity : entities) {
            Vector entityLocation = entity.getLocation().toVector();

            // Gets the "rate" or percentage of how far the entity
            // is from the explosion. For example, it the distance
            // is 8 and explosion radius is 10, the rate will be 4/5
            double impactRate = entityLocation.distance(vector) / damageRadius;

            if (impactRate > 1.0D) {
                debug.log(LogLevel.WARN, "Somehow an entity was damaged outside of the explosion's radius",
                        "is the server lagging?");
                continue;
            }

            Vector betweenEntityAndExplosion = entityLocation.subtract(vector);
            double distance = betweenEntityAndExplosion.length();

            // This should never be false due to double inaccuracy
            //if (distance != 0.0)

            // Normalize
            betweenEntityAndExplosion.multiply(1 / distance);

            double exposure = 1 /*getExposure(vector, entity)*/;
            double impact = (1 - impactRate) * exposure;

            temp.put(entity, impact);
        }

        return temp;
    }

    /**
     * Gets a double [0, 1] representing how exposed the entity is to the explosion
     *
     * @param vec3d Vector between explosion and entity
     * @param entity The entity exposed to the explosion
     * @return The level of exposure of the entity to the epxlosion
     */
    private static double getExposure(Vector vec3d, Entity entity) {
        HitBox box = CompatibilityAPI.getCompatibility().getProjectileCompatibility().getHitBox(entity);

        // Get the dimensions of the bounding box
        double width = box.getWidth();
        double height = box.getHeight();
        double depth = box.getDepth();

        // Gets the size of the grid in each axis
        double gridX = 1.0D / (width * 2.0D + 1.0D);
        double gridY = 1.0D / (height * 2.0D + 1.0D);
        double gridZ = 1.0D / (depth * 2.0D + 1.0D);

        // Outside of the grid
        if (gridX < 0 || gridY < 0 || gridZ < 0) return 0;

        double d3 = (1.0D - Math.floor(1.0D / gridX) * gridX) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / gridZ) * gridZ) / 2.0D;

        // Setup variables for the loop
        World world = entity.getWorld();
        Location loc = entity.getLocation();
        FluidCollisionMode mode = FluidCollisionMode.NEVER;

        int successfulTraces = 0;
        int totalTraces = 0;

        // For each grid on the bounding box
        for (double x = 0; x <= 1; x += gridX) {
            for (double y = 0; y <= 1; y += gridY) {
                for (double z = 0; z <= 1; z += gridZ) {
                    double a = NumberUtils.lerp(x, box.min.getX(), box.max.getX());
                    double b = NumberUtils.lerp(y, box.min.getY(), box.max.getY());
                    double c = NumberUtils.lerp(z, box.min.getZ(), box.max.getZ());

                    // Calculates a path from the origin of the explosion
                    // (0, 0, 0) to the current grid on the entity's bounding
                    // box. The Vector is then ray traced to check for obstructions
                    Vector vector = new Vector(a + d3, b, c + d4).subtract(vec3d);
                    RayTraceResult trace = world.rayTraceBlocks(loc, vector, vector.length(), mode);

                    // If the trace found no blocks
                    if (trace.getHitBlock() == null) {
                        successfulTraces++;
                    }

                    totalTraces++;
                }
            }
        }

        // The percentage of successful traces
        return ((double) successfulTraces) / totalTraces;
    }
}
