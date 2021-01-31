package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class PAmmoLeft extends PlaceholderHandler {

    public PAmmoLeft() {
        super("%ammo-left%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle) {
        if (itemStack == null) return null;

        Integer ammoLeft = TagHelper.getIntegerTag(itemStack, CustomTag.AMMO_LEFT);
        return ammoLeft == null ? null : ammoLeft.toString();
    }
}
