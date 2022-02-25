package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class PWeaponTitle extends PlaceholderHandler {

    public PWeaponTitle() {
        super("%weapon-title%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle, @Nullable EquipmentSlot slot) {
        return weaponTitle;
    }
}
