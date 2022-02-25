package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class PReload extends PlaceholderHandler {

    public PReload() {
        super("%reload%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle, @Nullable EquipmentSlot slot) {
        if (player == null || slot == null) return null;

        PlayerWrapper playerWrapper = WeaponMechanics.getPlayerWrapper(player);
        if (slot == EquipmentSlot.HAND ? playerWrapper.getMainHandData().isReloading() : playerWrapper.getOffHandData().isReloading()) {
            return WeaponMechanics.getBasicConfigurations().getString("Placeholder_Symbols.Reload");
        }

        return "";
    }
}
