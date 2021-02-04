package me.deecaad.weaponmechanics.weapon.explode.exposures;

import me.deecaad.core.utils.VectorUtils;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.Ray;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceCollision;
import me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceResult;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ExplosionShape;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Map;

import static me.deecaad.weaponmechanics.weapon.explode.raytrace.TraceCollision.ALL;

public interface ExplosionExposure {

    double FOV = Math.toRadians(70.0);

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
     * @param originLoc The point to check if the entity can see
     * @param entity The entity to check against
     * @param fov The field of view of the entity
     * @return true if the entity can see the origin
     */
    default boolean canSee(@Nonnull Location originLoc, @Nonnull LivingEntity entity, double fov) {

        // Get the vector between the entity and origin, and the player's eye
        // vector, and determine the angle between the 2 vectors.
        Vector origin = originLoc.toVector();
        Vector end = entity.getEyeLocation().toVector();
        Vector direction = entity.getLocation().getDirection();
        Vector between = origin.clone().subtract(end);
        double angle = VectorUtils.getAngleBetween(direction, between);

        // Check to see if the angle between the 2 vectors is smaller than the
        // entity's field of view. If the point is within the entity's fov,
        // then we need to do more calculations
        if (angle > fov) {
            return false;
        }

        Ray ray = new Ray(originLoc.getWorld(), origin, end, between.length());
        TraceCollision collision = new TraceCollision(ALL) {
            @Override
            public boolean canHit(Block block) {
                String name = block.getType().name();

                if (originLoc.getBlock().equals(block)) {
                    return false;
                }

                // THIN_GLASS
                // STAINED_GLASS_PANE
                // GLASS
                if (name.endsWith("GLASS") || name.endsWith("PANE")) {
                    return false;
                }

                // OAK_LEAVES
                // LEAVES
                else if (name.endsWith("LEAVES")) {
                    return false;
                }

                // BIRCH_FENCE_GATE
                // OAK_FENCE
                else if (name.endsWith("FENCE") || name.endsWith("FENCE_GATE")) {
                    return false;
                } else if (name.equals("SLIME_BLOCK")) {
                    return false;
                } else {
                    return super.canHit(block);
                }
            }

            @Override
            public boolean canHit(Entity entity1) {
                return !entity1.equals(entity);
            }
        };
        TraceResult result = ray.trace(collision, 0.2, true);

        // If there are no blocks/entities between the entity and the
        // origin, than the entity can see the explosion
        return result.isEmpty();
    }
}
