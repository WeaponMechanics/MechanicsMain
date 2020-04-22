package me.deecaad.core.effects.data;

import org.bukkit.entity.LivingEntity;

/**
 * This class holds data from a
 * <code>LivingEntity</code> before it is
 * damaged
 *
 * This is most useful for blood effects, and
 * displaying more/less based on the amount of
 * damage
 */
public class DamageData {

    private LivingEntity entity;
    private double damageAmount;

    public DamageData(LivingEntity entity, double damageAmount) {
        this.entity = entity;
        this.damageAmount = damageAmount;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public double getDamageAmount() {
        return damageAmount;
    }
}
