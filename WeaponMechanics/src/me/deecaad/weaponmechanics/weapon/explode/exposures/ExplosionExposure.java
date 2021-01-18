package me.deecaad.weaponmechanics.weapon.explode.exposures;

import me.deecaad.core.utils.VectorUtils;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.Ray;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceCollision;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceResult;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ExplosionShape;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Map;

public interface ExplosionExposure {

    double FOV = Math.PI / 2.0;

    /**
     * This method should return a list of entities that
     * are within this <code>Explosion</code> triggered
     * at the given <code>Location</code>.
     *
     * Conditions (Like player team, the cause of the
     * explosion, etc) are not used to filter entities,
     * that is handled separately
     *
     * The <code>Double</code> generic represents how much
     * "impact" the player gets. This should be a number (0, 1]
     * Higher numbers mean more damage and knockback
     *
     * @param origin Where the explosion spawns
     * @param shape The shape of the explosion
     * @return The effected players and their impact level
     */
    @Nonnull
    Map<LivingEntity, Double> mapExposures(@Nonnull Location origin, @Nonnull ExplosionShape shape);

    /**
     * Determines if the given entity can see the given <code>Location</code>. This
     * method assumes the entity's field of view is a 90 degree angle
     *
     * @param origin The point to check if the entity can see
     * @param entity The entity to check against
     * @return true if the entity can see the origin
     */
    default boolean canSee(@Nonnull Location origin, @Nonnull LivingEntity entity) {
        return canSee(origin, entity, FOV);
    }

    /**
     * Determines if the given entity can see the given <code>Location</code>.
     *
     * @param origin The point to check if the entity can see
     * @param entity The entity to check against
     * @param fov The field of view of the entity
     * @return true if the entity can see the origin
     */
    default boolean canSee(@Nonnull Location origin, @Nonnull LivingEntity entity, double fov) {

        Vector direction = entity.getLocation().getDirection();
        Vector between = origin.toVector().subtract(entity.getLocation().toVector());

        double angle = VectorUtils.getAngleBetween(direction, between);

        // Check to see if the angle between the 2 vectors is smaller than the
        // entity's field of view. If the point is within the entity's fov,
        // then we need to do more calculations
        if (angle > fov) {
            return false;
        }

        Ray ray = new Ray(origin.toVector(), between, origin.getWorld());
        TraceResult result = ray.trace(TraceCollision.BLOCK, 0.3);

        // If there are no blocks between the entity and the origin,
        // then the entity can see the origin
        return result.getBlocks() == null || result.getBlocks().isEmpty();
    }
}
