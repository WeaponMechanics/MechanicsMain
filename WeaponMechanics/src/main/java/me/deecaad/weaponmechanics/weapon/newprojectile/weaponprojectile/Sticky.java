package me.deecaad.weaponmechanics.weapon.newprojectile.weaponprojectile;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class Sticky {

    private ListHolder<Material> blocks;
    private ListHolder<EntityType> entities;

    public boolean handleSticking(WeaponProjectile projectile, RayTraceResult hit) {
        Double isValid = hit.isBlock() ? blocks.isValid(hit.getBlock().getType()) : entities.isValid(hit.getLivingEntity().getType());

        // Null means that it wasn't valid material or entity type
        if (isValid == null) {
            // Projectile should die
            return false;
        }

        projectile.setStickedData(new StickedData(hit));
        return true;
    }
}
