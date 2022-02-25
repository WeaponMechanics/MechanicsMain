package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoTypes;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getPlayerWrapper;

public class PAmmoAvailable extends PlaceholderHandler {

    public PAmmoAvailable() {
        super("%ammo-available%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle, @Nullable EquipmentSlot slot) {
        if (itemStack == null || weaponTitle == null) return null;

        AmmoTypes ammoTypes = getConfigurations().getObject(weaponTitle + ".Reload.Ammo.Ammo_Types", AmmoTypes.class);
        if (ammoTypes == null) return null;

        return "" + ammoTypes.getMaximumAmmo(itemStack, getPlayerWrapper(player), getConfigurations().getInt(weaponTitle + ".Reload.Magazine_Size"));
    }
}
