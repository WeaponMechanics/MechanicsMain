package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class PReload extends PlaceholderHandler {

    public PReload() {
        super("%reload%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle) {
        if (player == null) return null;

        IPlayerWrapper playerWrapper = WeaponMechanics.getPlayerWrapper(player);
        if (!playerWrapper.getMainHandData().isReloading() && !playerWrapper.getOffHandData().isReloading()) {
            return "";
        }

        return WeaponMechanics.getBasicConfigurations().getString("Placeholder_Symbols.Reload");
    }
}
