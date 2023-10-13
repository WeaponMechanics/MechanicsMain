package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

public class PReload extends PlaceholderHandler {

    public PReload() {
        super("reload");
    }

    @Nullable
    @Override
    public String onRequest(@NotNull PlaceholderData data) {
        if (data.player() == null || data.slot() == null) return null;

        PlayerWrapper playerWrapper = WeaponMechanics.getPlayerWrapper(data.player());
        if (data.slot() == EquipmentSlot.HAND ? playerWrapper.getMainHandData().isReloading() : playerWrapper.getOffHandData().isReloading()) {
            return WeaponMechanics.getBasicConfigurations().getString("Placeholder_Symbols.Reload");
        }

        return "";
    }
}
