package me.deecaad.weaponmechanics.events;

import org.bukkit.entity.LivingEntity;

public class EntityToggleWalkEvent extends WeaponMechanicsEvent {

    private LivingEntity livingEntity;
    private boolean isWalking;

    /**
     * Called when player starts or stops walking.
     *
     * @param livingEntity the living entity used in event
     * @param isWalking is walking
     */
    public EntityToggleWalkEvent(LivingEntity livingEntity, boolean isWalking) {
        this.livingEntity = livingEntity;
        this.isWalking = isWalking;
    }

    /**
     * @return the living entity
     */
    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }

    /**
     * @return true if player starts walking and false if stops walking
     */
    public boolean isWalking() {
        return this.isWalking;
    }
}