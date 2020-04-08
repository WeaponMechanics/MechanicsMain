package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ReloadHandler {

    private WeaponHandler weaponHandler;

    public ReloadHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * Tries to use reload
     *
     * @param entityWrapper the entity who used trigger
     * @param weaponTitle the weapon title
     * @param weaponStack the weapon stack
     * @param slot the slot used on trigger
     * @param triggerType the trigger type trying to activate reload
     * @return true if was able to reload
     */
    public boolean tryUse(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {

        return false;
    }

    public boolean startReload(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, boolean dualWield) {
        return false;
    }

    public boolean stopReload(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, boolean dualWield) {
        return false;
    }

    /**
     * Returns ammo left in weapon.
     * If returned value is -1, then ammo is not used in this weapon stack
     *
     * @param weaponStack the weapon stack
     * @return -1 if infinity, otherwise current ammo amount
     */
    public int getAmmoLeft(ItemStack weaponStack) {
        String ammoLeft = TagHelper.getCustomTag(weaponStack, CustomTag.AMMO_LEFT);
        if (ammoLeft == null) {
            // -1 means infinity
            return -1;
        }
        return Integer.parseInt(ammoLeft);
    }

    private boolean consumeAmmo(IEntityWrapper entityWrapper, ItemStack weaponStack, EquipmentSlot slot) {
        LivingEntity livingEntity = entityWrapper.getEntity();
        int ammoLeft = weaponHandler.getReloadHandler().getAmmoLeft(weaponStack);

        // -1 means infinite ammo
        if (ammoLeft != -1) {
            int ammoToSet = ammoLeft - 1;

            if (ammoToSet <= -1) {
                // Can't consume more ammo
                return false;
            }

            weaponStack = TagHelper.setCustomTag(weaponStack, CustomTag.AMMO_LEFT, "" + ammoToSet);

            if (entityWrapper instanceof IPlayerWrapper) {
                // Deny weapon going up & down constantly while shooting
                ((IPlayerWrapper) entityWrapper).setDenyNextSetSlotPacket(true);
            }

            EntityEquipment equipment = livingEntity.getEquipment();
            if (CompatibilityAPI.getVersion() < 1.09) {
                equipment.setItemInHand(weaponStack);
            } else if (slot == EquipmentSlot.HAND) {
                equipment.setItemInMainHand(weaponStack);
            } else {
                equipment.setItemInOffHand(weaponStack);
            }
        }
        return true;
    }
}