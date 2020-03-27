package me.deecaad.weaponmechanics.weapon.shoot;

import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ShootHandler {

    private WeaponHandler weaponHandler;

    public ShootHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * Tries to use shoot
     *
     * @param entityWrapper the entity who used trigger
     * @param weaponTitle the weapon title
     * @param weaponStack the weapon stack
     * @param slot the slot used on trigger
     * @param triggerType the trigger type trying to activate shoot
     * @return true if was able to shoot
     */
    public boolean tryUse(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType) {

        return false;
    }
}