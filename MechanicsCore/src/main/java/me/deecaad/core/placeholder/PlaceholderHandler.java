package me.deecaad.core.placeholder;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Locale;

public abstract class PlaceholderHandler {

    private final String placeholderName;

    /**
     * Creates new placeholder handler instance
     *
     * @param placeholderName the placeholder (for example %my_placeholder%)
     */
    public PlaceholderHandler(String placeholderName) {
        this.placeholderName = PlaceholderAPI.addPercentSigns(placeholderName).toLowerCase(Locale.ROOT);
    }

    /**
     * For example this could be %my_placeholder% or %this_is_my_other_placeholder%
     *
     * @return the placeholder name
     */
    public String getPlaceholderName() {
        return this.placeholderName;
    }

    /**
     * Used to modify placeholder result when this specific placeholder is being requested.
     * Remember to check nulls!
     *
     * @param player the player involved in this request, can be null
     * @param itemStack the item stack involved in this request, can be null
     * @param itemTitle the item title involved in this request, can be null
     * @param slot the weapon slot involved in this request, can be null
     * @return the result for placeholder or null
     */
    @Nullable
    public abstract String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String itemTitle, @Nullable EquipmentSlot slot);
}
