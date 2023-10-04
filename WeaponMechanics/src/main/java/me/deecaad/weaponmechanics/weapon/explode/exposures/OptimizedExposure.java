package me.deecaad.weaponmechanics.weapon.explode.exposures;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.Ray;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceCollision;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceResult;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ExplosionShape;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class OptimizedExposure implements ExplosionExposure {

    @NotNull
    @Override
    public DoubleMap<LivingEntity> mapExposures(@NotNull Location origin, @NotNull ExplosionShape shape) {

        List<LivingEntity> entities = shape.getEntities(origin);
        DoubleMap<LivingEntity> temp = new DoubleMap<>(entities.size());

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
            // is from the explosion. For example, if the distance
            // is 8 and explosion radius is 10, the rate will be 1/5
            Vector between = entityLocation.subtract(vector);
            double distance = between.length();
            double impactRate = (damageRadius - distance) / damageRadius;
            if (impactRate > 1.0D) {
                debug.log(LogLevel.DEBUG, "Entity " + entity + " was just outside the blast radius");
                continue;
            }

            Vector betweenEntityAndExplosion = entityLocation.subtract(vector);

            // If there is distance between the entity and the explosion
            if (distance != 0.0) {

                // Normalize
                betweenEntityAndExplosion.multiply(1.0 / distance);

                double exposure = getExposure(vector, entity);
                double impact = impactRate * exposure;

                temp.put(entity, impact);
            }
        }

        return temp;
    }

    /**
     * Gets a double [0.0, 1.0] representing how exposed the entity is to the explosion.
     * Exposure is determined by 8 rays, 1 ray for each corner of an entity's
     * bounding box. The returned exposure is equal to the number of rays that hit
     * the entity divided by 8.
     *
     * <p>There is also one ray going to the center of the entity hit-box that
     * has the power of 4 rays.
     *
     * @param vec3d The origin point
     * @param entity The entity exposed to the explosion
     * @return The level of exposure of the entity to the explosion
     */
    private static double getExposure(Vector vec3d, Entity entity) {
        HitBox box = CompatibilityAPI.getEntityCompatibility().getHitBox(entity);

        if (box == null) {
            return 0.0;
        }

        // Setup variables for the loop
        World world = entity.getWorld();
        Vector min = box.getMin();
        Vector max = box.getMax();

        int successfulTraces = 0;
        int totalTraces = 0;

        // For each corner of the bounding box
        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = 0; z <= 1; z++) {
                    Vector lerp = VectorUtil.lerp(min, max, x, y, z);

                    // Determine if the ray can hit the entity without hitting a block
                    Ray ray = new Ray(world, vec3d, lerp);
                    TraceResult trace = ray.trace(TraceCollision.BLOCK, 0.3);
                    if (trace.getBlocks().isEmpty()) {
                        successfulTraces++;
                    }

                    totalTraces++;
                }
            }
        }

        // Add one more ray pointing to the center of the bound box. If this
        // ray hits the entity, it has the power of 4 rays. If this ray does
        // not hit the entity, it has the power of 0 rays
        Ray ray = new Ray(world, vec3d, VectorUtil.lerp(min, max, 0.5));
        TraceResult trace = ray.trace(TraceCollision.BLOCK, 0.3);
        if (trace.getBlocks().isEmpty()) {
            successfulTraces += 4;
            totalTraces += 4;
        }

        // The percentage of successful traces
        return ((double) successfulTraces) / totalTraces;
    }
}
