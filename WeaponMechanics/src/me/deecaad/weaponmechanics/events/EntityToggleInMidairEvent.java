package me.deecaad.weaponmechanics.events;

import org.bukkit.entity.LivingEntity;

public class EntityToggleInMidairEvent extends WeaponMechanicsEvent {

    private LivingEntity livingEntity;
    private boolean isInMidair;

    /**
     * Called when player goes midair or lands.
     *
     * @param livingEntity the livingEntity used in event
     * @param isInMidair is in midair
     */
    public EntityToggleInMidairEvent(LivingEntity livingEntity, boolean isInMidair) {
        this.livingEntity = livingEntity;
        this.isInMidair = isInMidair;
    }

    /**
     * @return the living entity
     */
    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }

    /**
     * @return true if player goes midair and false if lands
     */
    public boolean isInMidair() {
        return this.isInMidair;
    }
}