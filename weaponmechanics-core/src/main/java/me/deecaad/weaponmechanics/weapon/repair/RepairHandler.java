package me.deecaad.weaponmechanics.weapon.repair;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Handles applying repair kits to weapons from inventory interactions.
 */
public class RepairHandler {

    private final WeaponHandler weaponHandler;

    public RepairHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    /**
     * Attempts to apply {@code kitStack} to {@code weaponStack}.
     *
     * @param playerWrapper the player performing the repair
     * @param weaponStack the clicked weapon
     * @param kitStack the repair kit held by the inventory cursor
     * @param weaponSlot the equipped hand containing the weapon, or {@code null} when the clicked
     *                   weapon is not currently equipped
     * @return whether the click was unrelated, denied, or repaired successfully
     */
    public RepairResult tryRepair(PlayerWrapper playerWrapper, ItemStack weaponStack, ItemStack kitStack, @Nullable EquipmentSlot weaponSlot) {
        RepairConfig repairConfig = WeaponMechanics.getInstance().getConfiguration().getObject("Repair", RepairConfig.class);
        if (repairConfig == null || !repairConfig.isEnabled())
            return RepairResult.NOT_APPLICABLE;

        if (weaponStack == null || kitStack == null || weaponStack.getAmount() <= 0 || kitStack.getAmount() <= 0) {
            return RepairResult.NOT_APPLICABLE;
        }

        String weaponTitle = weaponHandler.getInfoHandler().getWeaponTitle(weaponStack, false);
        if (weaponTitle == null || !CustomTag.REPAIR_KIT.hasString(kitStack))
            return RepairResult.NOT_APPLICABLE;

        String kitName = CustomTag.REPAIR_KIT.getString(kitStack);
        RepairKit kit = repairConfig.getKit(kitName);
        if (kit == null)
            return RepairResult.DENIED;

        Configuration weaponConfig = WeaponMechanics.getInstance().getWeaponConfigurations();
        List<?> allowedKits = weaponConfig.getObject(weaponTitle + ".Repair.Allowed_Kits", Collections.emptyList(), List.class);
        if (!allowedKits.contains(kitName))
            return RepairResult.DENIED;

        ItemMeta meta = weaponStack.getItemMeta();
        if (!(meta instanceof Damageable damageable) || !damageable.hasMaxDamage())
            return RepairResult.DENIED;

        int currentDamage = damageable.getDamage();
        if (currentDamage <= 0 && !repairConfig.isAllowRepairWhileFull())
            return RepairResult.DENIED;

        HandData handData = getHandData(playerWrapper, weaponSlot);
        if (handData != null) {
            if (!repairConfig.isAllowRepairWhileReloading() && handData.isReloading())
                return RepairResult.DENIED;

            if (!repairConfig.isAllowRepairWhileShooting() && (handData.isUsingFullAuto() || handData.isUsingBurst())) {
                return RepairResult.DENIED;
            }
        }

        if (!repairConfig.isExcessRepairWasted() && kit.getRepairAmount() > currentDamage)
            return RepairResult.DENIED;

        int newDamage = Math.max(0, currentDamage - kit.getRepairAmount());
        damageable.setDamage(newDamage);
        weaponStack.setItemMeta(meta);

        if (kit.isConsumeItem())
            kitStack.setAmount(kitStack.getAmount() - 1);

        return RepairResult.REPAIRED;
    }

    private HandData getHandData(PlayerWrapper playerWrapper, @Nullable EquipmentSlot weaponSlot) {
        if (weaponSlot == EquipmentSlot.HAND)
            return playerWrapper.getMainHandData();
        if (weaponSlot == EquipmentSlot.OFF_HAND)
            return playerWrapper.getOffHandData();
        return null;
    }
}
