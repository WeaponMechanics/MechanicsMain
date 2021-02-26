package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class WeaponReloadCompleteEvent extends WeaponEvent {

    public WeaponReloadCompleteEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser) {
        super(weaponTitle, weaponItem, weaponUser);
    }
}