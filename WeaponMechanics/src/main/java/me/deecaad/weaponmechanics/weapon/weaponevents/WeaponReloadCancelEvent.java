package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This class outlines the event of an entity cancelling a reload. This may be
 * due to switching items, or attempting to shoot.
 *
 * @see WeaponReloadEvent
 */
public class WeaponReloadCancelEvent extends WeaponEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int elapsedTime;

    public WeaponReloadCancelEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, int elapsedTime) {
        super(weaponTitle, weaponItem, weaponUser);
        this.elapsedTime = elapsedTime;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
