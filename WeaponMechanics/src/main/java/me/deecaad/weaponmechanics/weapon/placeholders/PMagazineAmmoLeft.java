package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.utils.CustomTag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PMagazineAmmoLeft extends PlaceholderHandler {

    public PMagazineAmmoLeft() {
        super("%magazine-ammo-left%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle) {
        if (itemStack == null) return null;
        return "" + CustomTag.MAGAZINE_AMMO_LEFT.getInteger(itemStack);
    }
}