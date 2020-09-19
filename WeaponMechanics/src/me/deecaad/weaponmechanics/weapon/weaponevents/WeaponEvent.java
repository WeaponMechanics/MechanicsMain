package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.events.WeaponMechanicsEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class WeaponEvent extends WeaponMechanicsEvent {

    protected final String weaponTitle;
    private final ItemStack weaponItem;
    private final LivingEntity weaponUser;

    /**
     * Called when any weapon event is called.
     *
     * @param weaponTitle the weapon name used in event
     */
    public WeaponEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser) {
        this.weaponTitle = weaponTitle;
        this.weaponItem = weaponItem;
        this.weaponUser = weaponUser;
    }

    /**
     * @return the weapon title
     */
    public String getWeaponTitle() {
        return weaponTitle;
    }

    /**
     * @return The itemstack weapon
     */
    public ItemStack getWeaponItem() {
        return weaponItem;
    }

    /**
     * @return the living entity involved in event
     */
    public LivingEntity getWeaponUser() {
        return this.weaponUser;
    }
}