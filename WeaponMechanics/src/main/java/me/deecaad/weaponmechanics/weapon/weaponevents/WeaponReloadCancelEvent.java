package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * This class outlines the event of an entity cancelling a reload. This may be
 * due to switching items, or attempting to shoot.
 *
 * @see WeaponReloadEvent
 */
public class WeaponReloadCancelEvent extends WeaponEvent {

    private final int elapsedTime;

    public WeaponReloadCancelEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, int elapsedTime) {
        super(weaponTitle, weaponItem, weaponUser);
        this.elapsedTime = elapsedTime;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }
}
