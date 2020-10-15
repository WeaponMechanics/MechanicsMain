package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class WeaponReloadCancelEvent extends WeaponEvent {

    private final WeaponReloadEvent reloadEvent;
    private int elapsedTime;
    private boolean isCancelled;

    public WeaponReloadCancelEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, WeaponReloadEvent reloadEvent, int elapsedTime) {
        super(weaponTitle, weaponItem, weaponUser);
        this.reloadEvent = reloadEvent;
        this.elapsedTime = elapsedTime;
    }

    public WeaponReloadEvent getReloadEvent() {
        return reloadEvent;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public double getElapsedPercentage() {
        return ((double) elapsedTime) / ((double) reloadEvent.getReloadTime());
    }
}
