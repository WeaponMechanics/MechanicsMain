package me.deecaad.weaponmechanics.weapon.shoot;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

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
        Configuration config = getConfigurations();
        boolean mainHand = slot == EquipmentSlot.HAND;

        Trigger trigger = config.getObject(weaponTitle + ".Shoot.Trigger", Trigger.class);
        if (trigger == null) return false;

        return false;
    }
}