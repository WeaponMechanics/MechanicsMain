package me.deecaad.weaponmechanics.weapon.trigger;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoTypes;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getWeaponHandler;

public class AmmoTypeSwitchTriggerListener implements TriggerListener {

    @Override
    public boolean allowOtherTriggers() {
        return false;
    }

    @Override
    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield, @Nullable LivingEntity victim) {
        Configuration config = getConfigurations();
        Trigger ammoTypeSwitchTrigger = config.getObject(weaponTitle + ".Reload.Ammo.Ammo_Type_Switch.Trigger", Trigger.class);
        if (ammoTypeSwitchTrigger == null || entityWrapper.getEntity().getType() != EntityType.PLAYER
                || !ammoTypeSwitchTrigger.check(triggerType, slot, entityWrapper)) {
            return false;
        }

        AmmoTypes ammoTypes = config.getObject(weaponTitle + ".Reload.Ammo.Ammo_Types", AmmoTypes.class);
        if (ammoTypes == null) return false;

        // First empty the current ammo
        int ammoLeft = CustomTag.AMMO_LEFT.getInteger(weaponStack);
        if (ammoLeft > 0) {
            ammoTypes.giveAmmo(weaponStack, (PlayerWrapper) entityWrapper, ammoLeft, config.getInt(weaponTitle + ".Reload.Magazine_Size"));
            CustomTag.AMMO_LEFT.setInteger(weaponStack, 0);
        }

        // Then do the switch
        ammoTypes.updateToNextAmmoType(weaponStack);

        entityWrapper.getMainHandData().cancelTasks();
        entityWrapper.getOffHandData().cancelTasks();

        Mechanics ammoTypeSwitchMechanics = getConfigurations().getObject(weaponTitle + ".Reload.Ammo.Ammo_Type_Switch.Mechanics", Mechanics.class);
        if (ammoTypeSwitchMechanics != null) ammoTypeSwitchMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

        WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
        if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, slot);

        getWeaponHandler().getSkinHandler().tryUse(triggerType, entityWrapper, weaponTitle, weaponStack, slot);

        return true;
    }
}
