package me.deecaad.weaponmechanics.weapon.newprojectile.weaponprojectile;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Bouncy {

    public static final double REQUIRED_MOTION_TO_BOUNCE = 0.3;

    private int maximumBounceAmount;
    private ListHolder<Material> blocks;
    private ListHolder<EntityType> entities;

    /**
     * @param projectile the projectile
     * @param hit the hit entity or block
     * @return true if projectile bounced, false if projectile should die
     */
    public boolean handleBounce(WeaponProjectile projectile, RayTraceResult hit) {
        if (projectile.getMotionLength() < REQUIRED_MOTION_TO_BOUNCE) return false;

        Double speedModifier = hit.isBlock() ? blocks.isValid(hit.getBlock().getType()) : entities.isValid(hit.getLivingEntity().getType());

        // Speed modifier null would mean that it wasn't valid material or entity type
        if (speedModifier == null || maximumBounceAmount - projectile.getBounces() < 0) {
            // Projectile should die
            return false;
        }

        Vector motion = projectile.getMotion();
        if (speedModifier != 1.0) motion.multiply(speedModifier);

        switch (hit.getHitFace()) {
            case UP: case DOWN:
                motion.setY(-motion.getY());
                break;
            case EAST: case WEST:
                motion.setX(-motion.getX());
                break;
            case NORTH: case SOUTH:
                motion.setZ(-motion.getZ());
                break;
            default:
                break;
        }

        projectile.setMotion(motion);

        return true;
    }
}