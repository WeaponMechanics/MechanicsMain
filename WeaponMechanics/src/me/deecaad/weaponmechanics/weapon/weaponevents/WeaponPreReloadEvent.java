package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

/**
 * This class outlines an event before reload calculations take place. This
 * event is great for cancelling weapon reloads.
 */
public class WeaponPreReloadEvent extends WeaponEvent implements Cancellable {

    private boolean isCancelled;

    public WeaponPreReloadEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser) {
        super(weaponTitle, weaponItem, weaponUser);
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
