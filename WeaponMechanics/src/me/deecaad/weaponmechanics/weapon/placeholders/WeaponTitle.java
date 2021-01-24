package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.placeholder.PlaceholderHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class WeaponTitle extends PlaceholderHandler {

    public WeaponTitle() {
        super("weapon-title");
        PlaceholderAPI.addPlaceholderHandler(this);
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle) {
        return weaponTitle;
    }
}
