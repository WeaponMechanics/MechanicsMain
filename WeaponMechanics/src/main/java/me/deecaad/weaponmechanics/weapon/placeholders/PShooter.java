package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class PShooter extends PlaceholderHandler {

    public PShooter() {
        super("%shooter%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle) {
        return player != null ? player.getName() : null;
    }
}
