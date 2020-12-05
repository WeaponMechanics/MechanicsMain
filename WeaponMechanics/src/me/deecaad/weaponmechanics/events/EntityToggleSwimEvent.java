package me.deecaad.weaponmechanics.events;

import org.bukkit.entity.LivingEntity;

public class EntityToggleSwimEvent extends WeaponMechanicsEvent {

    private LivingEntity livingEntity;
    private boolean isSwimming;

    /**
     * Called when player starts or stops swimming
     *
     * @param livingEntity the livingEntity used in event
     * @param isSwimming   is swimming
     */
    public EntityToggleSwimEvent(LivingEntity livingEntity, boolean isSwimming) {
        this.livingEntity = livingEntity;
        this.isSwimming = isSwimming;
    }

    /**
     * @return the living entity
     */
    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }

    /**
     * @return true if player goes swimming and false if stops
     */
    public boolean isSwimming() {
        return this.isSwimming;
    }
}