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
            // Strings from tags, we hve to parse the
            // String to an Integer.
            return (str == null) ? null : Integer.valueOf(str);
        }
    }

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
