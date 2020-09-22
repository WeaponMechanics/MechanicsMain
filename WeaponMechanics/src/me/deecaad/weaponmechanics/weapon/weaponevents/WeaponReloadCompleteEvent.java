package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class WeaponReloadCompleteEvent extends WeaponEvent{

    private WeaponReloadEvent reloadEvent;
    private List<WeaponReloadCancelEvent> cancelEvents;

    public WeaponReloadCompleteEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser,
                                     WeaponReloadEvent reloadEvent, @Nullable List<WeaponReloadCancelEvent> cancelEvents) {

        super(weaponTitle, weaponItem, weaponUser);
        this.reloadEvent = reloadEvent;
        this.cancelEvents = cancelEvents;
    }

    public int getReloadTime() {
        return reloadEvent.getReloadTime();
    }

    public int getReloadAmount() {
        return reloadEvent.getReloadAmount();
    }

    public int getMagazineSize() {
        return reloadEvent.getMagazineSize();
    }

    public WeaponReloadEvent getReloadEvent() {
        return reloadEvent;
    }

    @Nullable
    public List<WeaponReloadCancelEvent> getCancelEvents() {
        return cancelEvents;
    }
}
