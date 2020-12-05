package me.deecaad.weaponmechanics.events;

import org.bukkit.entity.LivingEntity;

public class EntityToggleStandEvent extends WeaponMechanicsEvent {

    private LivingEntity livingEntity;
    private boolean isStanding;

    /**
     * Called when player starts or stops standing.
     *
     * @param livingEntity the livingEntity used in event
     * @param isStanding   is standing
     */
    public EntityToggleStandEvent(LivingEntity livingEntity, boolean isStanding) {
        this.livingEntity = livingEntity;
        this.isStanding = isStanding;
    }

    /**
     * @return the living entity
     */
    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }

    /**
     * @return true if player starts standing and false if stops standing
     */
    public boolean isStanding() {
        return this.isStanding;
    }
}