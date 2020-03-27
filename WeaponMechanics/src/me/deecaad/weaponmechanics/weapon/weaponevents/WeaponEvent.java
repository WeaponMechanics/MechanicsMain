package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.events.WeaponMechanicsEvent;
import org.bukkit.entity.LivingEntity;

public class WeaponEvent extends WeaponMechanicsEvent {

    protected final String weaponTitle;
    private LivingEntity livingEntity;

    /**
     * Called when any weapon event is called.
     *
     * @param weaponTitle the weapon name used in event
     */
    public WeaponEvent(String weaponTitle, LivingEntity livingEntity) {
        this.weaponTitle = weaponTitle;
        this.livingEntity = livingEntity;
    }

    /**
     * @return the weapon title
     */
    public final String getWeaponTitle() {
        return weaponTitle;
    }

    /**
     * @return the living entity involved in event
     */
    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }
}