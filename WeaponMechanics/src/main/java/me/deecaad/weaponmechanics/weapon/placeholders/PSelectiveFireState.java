package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.utils.CustomTag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public class PSelectiveFireState extends PlaceholderHandler {

    public PSelectiveFireState() {
        super("%selective_fire_state%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle, @Nullable EquipmentSlot slot) {
        if (itemStack == null) return null;

        int selectiveFireState = CustomTag.SELECTIVE_FIRE.getInteger(itemStack);

        switch (selectiveFireState) {
            case (1):
                return getBasicConfigurations().getString("Placeholder_Symbols.Selective_Fire.BURST");
            case (2):
                return getBasicConfigurations().getString("Placeholder_Symbols.Selective_Fire.AUTO");
            default:
                return getBasicConfigurations().getString("Placeholder_Symbols.Selective_Fire.SINGLE");
        }
    }
}