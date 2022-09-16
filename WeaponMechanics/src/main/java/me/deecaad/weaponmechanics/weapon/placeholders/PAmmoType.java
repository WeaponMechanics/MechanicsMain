package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoTypes;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class PAmmoType extends PlaceholderHandler {

    public PAmmoType() {
        super("%ammo-type%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle, @Nullable EquipmentSlot slot) {
        if (itemStack == null || weaponTitle == null) return null;

        AmmoTypes ammoTypes = getConfigurations().getObject(weaponTitle + ".Reload.Ammo.Ammo_Types", AmmoTypes.class);

        // Simply don't show anything
        if (ammoTypes == null) return "";

        return ammoTypes.getCurrentAmmoSymbol(itemStack);
    }
}
