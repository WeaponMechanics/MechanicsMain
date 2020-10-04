package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class WeaponUnloadEvent extends WeaponEvent implements Cancellable {

    private int unloadAmount;
    private int magazineSize;
    private boolean isCancelled;

    public WeaponUnloadEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, int unloadAmount, int magazineSize) {
        super(weaponTitle, weaponStack, shooter);
        this.unloadAmount = unloadAmount;
        this.magazineSize = magazineSize;
    }

    public int getUnloadAmount() {
        return unloadAmount;
    }

    public void setUnloadAmount(int unloadAmount) {
        this.unloadAmount = unloadAmount;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public void setMagazineSize(int magazineSize) {
        this.magazineSize = magazineSize;
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
