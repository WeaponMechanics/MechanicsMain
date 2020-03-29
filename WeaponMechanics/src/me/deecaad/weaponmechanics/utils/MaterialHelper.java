package me.deecaad.weaponmechanics.utils;

import me.deecaad.compatibility.CompatibilityAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Made this into external class in case we decide to use XMaterial resource so its easier to start using it when we only have to modify this class
 */
public class MaterialHelper {
    
    /**
     * Don't let anyone instantiate this class
     */
    private MaterialHelper() {
    }
    
    /**
     * Simple method to convert string to item stack.
     *
     * @param itemstackString the string containing item stack
     * @return the item stack generated from string
     */
    public static ItemStack fromStringToItemStack(String itemstackString) {
        if (CompatibilityAPI.getVersion() < 1.13) {
            String[] splitted = itemstackString.split(":");
            return new ItemStack(Material.valueOf(splitted[0].toUpperCase()), 1, splitted.length > 1 ? Short.parseShort(splitted[1]) : 0);
        }
        return new ItemStack(Material.valueOf(itemstackString.toUpperCase()));
    }
}