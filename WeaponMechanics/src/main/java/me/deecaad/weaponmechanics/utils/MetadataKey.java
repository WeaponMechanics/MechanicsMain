package me.deecaad.weaponmechanics.utils;

import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

public enum MetadataKey {

    /**
     * Set this metadata and check it on EntityDamageByEntityEvent to deny
     * unintentional melee casts and unintentional damage cancel. LivingEntity.damage()
     * always uses ENTITY_ATTACK as DamageCause. Using NMS to change damage cause would require
     * spawning of actual entity projectile.
     */
    VANILLA_DAMAGE("wm_vanilla_dmg"),

    CANCELLED_DAMAGE("wm_cancelled_dmg");

    private final String id;

    MetadataKey(String id) {
        this.id = id;
    }

    public boolean has(Entity entity) {
        return entity.hasMetadata(id);
    }

    /**
     * Sets the {@link FixedMetadataValue} for entity with given object
     */
    public void set(Entity entity, Object object) {
        entity.setMetadata(id, new FixedMetadataValue(WeaponMechanics.getPlugin(), object));
    }

    public Object get(Entity entity) {
        return entity.getMetadata(id);
    }

    public void remove(Entity entity) {
        entity.removeMetadata(id, WeaponMechanics.getPlugin());
    }
}