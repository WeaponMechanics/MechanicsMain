package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponcompatibility.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionShape;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.Ray;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceCollision;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceResult;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class DefaultExposure implements ExplosionExposure {

    @Nonnull
    @Override
    public Map<LivingEntity, Double> mapExposures(@Nonnull Location origin, @Nonnull ExplosionShape shape) {

        List<LivingEntity> entities = shape.getEntities(origin);

        // Map to store all the calculated entities in
        Map<LivingEntity, Double> temp = new HashMap<>();

        // How far away from the explosion to damage players
        double damageRadius = shape.getMaxDistance() * 2.0F;

        // Gets data on the location of the explosion
        World world = origin.getWorld();
        double x = origin.getX();
        double y = origin.getY();
        double z = origin.getZ();

        if (world == null) {
            debug.log(LogLevel.ERROR, "Explosion in null world? Location: " + origin, "Please report error to devs");
            return temp;
        }

        Vector vector = new Vector(x, y, z);

        for (LivingEntity entity : entities) {
            Vector entityLocation = entity.getLocation().toVector();

            // Gets the "rate" or percentage of how far the entity
            // is from the explosion. For example, it the distance
            // is 8 and explosion radius is 10, the rate will be 1/5
            double impactRate = (damageRadius - entityLocation.distance(vector)) / damageRadius;

            if (impactRate > 1.0D) {
                debug.log(LogLevel.WARN, "Somehow an entity was damaged outside of the explosion's radius",
                        "is the server lagging?");
                continue;
            }

            Vector betweenEntityAndExplosion = entityLocation.subtract(vector);
            double distance = betweenEntityAndExplosion.length();

            // If there is distance between the entity and the explosion
            if (distance != 0.0) {

                // Normalize
                betweenEntityAndExplosion.multiply(1 / distance);

                double exposure = getExposure(vector, entity);
                double impact = impactRate * exposure;

                temp.put(entity, impact);
            }
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
        HitBox box = WeaponCompatibilityAPI.getProjectileCompatibility().getHitBox(entity);

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

                    Ray ray = new Ray(vec3d, vector);
                    TraceResult trace = ray.trace(world, TraceCollision.BLOCK, 0.3); // todo changable in config
                    if (trace.getBlocks().isEmpty()) {
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
