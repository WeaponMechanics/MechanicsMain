package me.deecaad.weaponmechanics.weapon.newprojectile.weaponprojectile;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class Through {

    private int maximumThroughAmount;
    private ListHolder<Material> blocks;
    private ListHolder<EntityType> entities;

    /**
     * @param projectile the projectile
     * @param hit the hit entity or block
     * @return true if projectile went through, false if projectile should die
     */
    public boolean handleThrough(WeaponProjectile projectile, RayTraceResult hit) {

        Double speedModifier = hit.isBlock() ? blocks.isValid(hit.getBlock().getType()) : entities.isValid(hit.getLivingEntity().getType());

        // Speed modifier null would mean that it wasn't valid material or entity type
        if (speedModifier == null || maximumThroughAmount - projectile.getThroughAmount() < 0) {
            // Projectile should die
            return false;
        }

        if (speedModifier != 1.0) projectile.setMotion(projectile.getMotion().multiply(speedModifier));

        return true;
    }
}
