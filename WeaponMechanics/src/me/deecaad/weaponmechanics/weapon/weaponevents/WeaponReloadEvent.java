package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class WeaponReloadEvent extends WeaponEvent {

    private int reloadTime;
    private int reloadAmount;
    private int magazineSize;

    public WeaponReloadEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, int reloadTime, int reloadAmount, int magazineSize) {
        super(weaponTitle, weaponItem, weaponUser);
        this.reloadTime = reloadTime;
        this.reloadAmount = reloadAmount;
        this.magazineSize = magazineSize;
    }

    public int getReloadTime() {
        return reloadTime;
    }

    public void setReloadTime(int reloadTime) {
        this.reloadTime = reloadTime;
    }

    public int getReloadAmount() {
        return reloadAmount;
    }

    public void setReloadAmount(int reloadAmount) {
        this.reloadAmount = reloadAmount;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public void setMagazineSize(int magazineSize) {
        this.magazineSize = magazineSize;
    }
}
