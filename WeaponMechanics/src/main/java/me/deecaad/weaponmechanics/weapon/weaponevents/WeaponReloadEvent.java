package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This class outlines the event of an entity starting to reload a weapon.
 */
public class WeaponReloadEvent extends WeaponEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private int reloadTime;
    private int reloadAmount;
    private int magazineSize;
    private int firearmOpenTime;
    private int firearmCloseTime;

    public WeaponReloadEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, int reloadTime, int reloadAmount, int magazineSize,
                             int firearmOpenTime, int firearmCloseTime) {
        super(weaponTitle, weaponItem, weaponUser);
        this.reloadTime = reloadTime;
        this.reloadAmount = reloadAmount;
        this.magazineSize = magazineSize;
        this.firearmOpenTime = firearmOpenTime;
        this.firearmCloseTime = firearmCloseTime;
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

    public int getFirearmOpenTime() {
        return firearmOpenTime;
    }

    public void setFirearmOpenTime(int firearmOpenTime) {
        this.firearmOpenTime = firearmOpenTime;
    }

    public int getFirearmCloseTime() {
        return firearmCloseTime;
    }

    public void setFirearmCloseTime(int firearmCloseTime) {
        this.firearmCloseTime = firearmCloseTime;
    }

    public int getReloadCompleteTime() {
        return firearmOpenTime + reloadTime + firearmCloseTime;
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
