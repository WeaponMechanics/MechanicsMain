package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class WeaponHeldEvent extends WeaponEvent implements Cancellable {

    private final int slot;
    private boolean isCancelled;

    public WeaponHeldEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, int slot) {
        super(weaponTitle, weaponStack, shooter);
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
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
