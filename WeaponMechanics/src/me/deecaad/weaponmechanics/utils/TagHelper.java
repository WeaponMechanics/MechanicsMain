package me.deecaad.weaponmechanics.utils;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;

public class TagHelper {

    /**
     * Don't let anyone instantiate this class
     */
    private TagHelper() {
    }

    /**
     * @param itemStack the item stack from which to get tag
     * @param tag the tag name
     * @return the value of the tag, if not found null
     */
    public static String getCustomTag(ItemStack itemStack, CustomTag tag) {
        return getCustomTag(itemStack, tag.getId());
    }

    /**
     * Set or change tag of item stack with given value.
     *
     * @param itemStack the item stack to modify
     * @param tag the tag name
     * @param value the value for tag
     * @return the item stack with new or modified tag value
     */
    public static ItemStack setCustomTag(ItemStack itemStack, CustomTag tag, String value) {
        return setCustomTag(itemStack, tag.getId(), value);
    }

    /**
     * @param itemStack the item stack from which to get tag
     * @param tag the tag name
     * @return the value of the tag (may be null)
     */
    public static String getCustomTag(ItemStack itemStack, String tag) {

        // 1.14 R1 and above PersistentDataContainer
        if (CompatibilityAPI.getVersion() >= 1.141) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(WeaponMechanics.getPlugin(), tag);
            return itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING) ? itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING) : null;
        }

        // 1.13 R2 CustomItemTagContainer
        if (CompatibilityAPI.getVersion() >= 1.132) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(WeaponMechanics.getPlugin(), tag);
            return itemMeta.getCustomTagContainer().hasCustomTag(key, ItemTagType.STRING) ? itemMeta.getCustomTagContainer().getCustomTag(key, ItemTagType.STRING) : null;
        }

        // 1.13 R1 and lower full NMS
        return CompatibilityAPI.getCompatibility().getNBTCompatibility().getCustomTag(itemStack, tag);
    }

    /**
     * Set or change tag of item stack with given value.
     *
     * @param itemStack the item stack to modify
     * @param tag the tag name
     * @param value the value for tag
     * @return the item stack with new or modified tag value
     */
    public static ItemStack setCustomTag(ItemStack itemStack, String tag, String value) {

        // 1.14 R1 and above PersistentDataContainer
        if (CompatibilityAPI.getVersion() >= 1.141) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(WeaponMechanics.getPlugin(), tag);
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        // 1.13 R2 CustomItemTagContainer
        if (CompatibilityAPI.getVersion() >= 1.132) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(WeaponMechanics.getPlugin(), tag);
            itemMeta.getCustomTagContainer().setCustomTag(key, ItemTagType.STRING, value);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        // 1.13 R1 and lower full NMS
        return CompatibilityAPI.getCompatibility().getNBTCompatibility().setCustomTag(itemStack, tag, value);
    }

    /**
     * Adds attribute for item stack with given amount.
     * This method keeps other attributes set by for example commands or other plugins, BUT
     * overrides WeaponMechanics old attribute value if found.
     *
     * @param itemStack the item stack to which attribute will be set
     * @param attributeType the attribute type
     * @param amount the amount for attribute type
     * @return the item stack with attribute
     */
    public static ItemStack setAttributeValue(ItemStack itemStack, AttributeType attributeType, double amount) {
        if (CompatibilityAPI.getVersion() >= 1.132) { // 1.13 R2 has API for this
            ItemMeta itemMeta = itemStack.getItemMeta();
            Attribute attribute = Attribute.valueOf(attributeType.name());

            AttributeModifier hand = new AttributeModifier(attributeType.getMainhandUUID(), "WM_Main", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            AttributeModifier offhand = new AttributeModifier(attributeType.getOffhandUUID(), "WM_Off", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);

            // API doesn't allow modifying AttributeModifiers so I have to delete old ones based on their UUIDs
            // and add these new AttributeModifiers which contain new amount for the Attribute
            itemMeta.removeAttributeModifier(attribute, hand);
            itemMeta.removeAttributeModifier(attribute, offhand);

            itemMeta.addAttributeModifier(attribute, hand);
            itemMeta.addAttributeModifier(attribute, offhand);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        // 1.13 R1 and lower full NMS
        return CompatibilityAPI.getCompatibility().getNBTCompatibility().setAttributeValue(itemStack, attributeType, amount);
    }
}