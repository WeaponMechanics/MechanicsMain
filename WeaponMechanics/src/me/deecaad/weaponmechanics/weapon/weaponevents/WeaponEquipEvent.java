package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class WeaponEquipEvent extends WeaponEvent {

    private final boolean mainHand;

    public WeaponEquipEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, boolean mainHand) {
        super(weaponTitle, weaponStack, shooter);
        this.mainHand = mainHand;
    }

    public boolean isMainHand() {
        return mainHand;
    }

    public boolean isOffHand() {
        return !mainHand;
    }
}