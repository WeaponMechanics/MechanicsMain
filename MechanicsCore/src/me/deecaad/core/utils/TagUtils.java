package me.deecaad.core.utils;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.MechanicsCore;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class to easily get and set different data types
 * stored in nbt tags
 */
public class TagUtils {

    /**
     * Don't let anyone instantiate this class
     */
    private TagUtils() {
    }

    /**
     * Gets the <code>Integer</code> stored in the given
     * NBT tag, "tag" held by the given item. If the item
     * does not have the tag, this method will return null
     *
     * @param item The item storing the tag
     * @param tag The tag to pull the Integer from
     * @return The value or null
     */
    @Nullable
    public static Integer getInteger(@Nonnull ItemStack item, @Nonnull String tag) {

        ItemMeta meta = item.getItemMeta();
        double version = CompatibilityAPI.getVersion();

        // 1.14 R1 and above PersistentDataContainer
        if (version >= 1.141) {
            PersistentDataContainer keys = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(MechanicsCore.instance, tag);

            boolean has = keys.has(key, PersistentDataType.INTEGER);
            return has ? keys.get(key, PersistentDataType.INTEGER) : null;
        }

        // 1.13 R2 CustomItemTagContainer
        else if (version >= 1.132) {
            NamespacedKey key = new NamespacedKey(MechanicsCore.instance, tag);
            CustomItemTagContainer keys = meta.getCustomTagContainer();

            boolean has = keys.hasCustomTag(key, ItemTagType.INTEGER);
            return has ? keys.getCustomTag(key, ItemTagType.INTEGER) : null;
        }

        // 1.13 R1 and lower NMS
        else {
            String str = CompatibilityAPI.getCompatibility().getNBTCompatibility().getCustomTag(item, tag);

            // Since NBT compatibility can only get/set
            // Strings from tags, we have to parse the
            // String to an Integer. This means that performance
            // will be better on versions 1.13r2 and higher
            return (str == null) ? null : Integer.valueOf(str);
        }
    }

    /**
     * Sets the <code>Integer</code> value of the given NBT tag
     * "tag" held by the given <code>ItemStack</code>. This method
     * returns the ItemStack with the NBT tag
     *
     * @param item The item to set the tag for
     * @param tag Which tag to set
     * @param value The value to set for the tag
     * @return The updated ItemStack with the set value for the tag
     */
    public static ItemStack setInteger(ItemStack item, @Nonnull String tag, Integer value) {

        ItemMeta meta = item.getItemMeta();
        double version = CompatibilityAPI.getVersion();


        // 1.14 R1 and above PersistentDataContainer
        if (version >= 1.141) {

            NamespacedKey key = new NamespacedKey(MechanicsCore.instance, tag);
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);

            item.setItemMeta(meta);
            return item;
        }

        // 1.13 R2 CustomItemTagContainer
        if (version >= 1.132) {

            NamespacedKey key = new NamespacedKey(MechanicsCore.instance, tag);
            meta.getCustomTagContainer().setCustomTag(key, ItemTagType.INTEGER, value);

            item.setItemMeta(meta);
            return item;
        }

        // 1.13 R1 and lower full NMS
        return CompatibilityAPI.getCompatibility().getNBTCompatibility().setCustomTag(item, tag, String.valueOf(value));
    }
}
