package me.deecaad.core.utils;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.MechanicsCore;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;

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
     * @param itemStack the item stack from which to get tag
     * @param tag       the tag name
     * @return the value of the tag (may be null)
     */
    public static String getStringTag(ItemStack itemStack, String tag) {

        // 1.14 R1 and above PersistentDataContainer
        if (CompatibilityAPI.getVersion() >= 1.141) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), tag);
            return itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING) ? itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING) : null;
        }

        // 1.13 R2 CustomItemTagContainer
        if (CompatibilityAPI.getVersion() >= 1.132) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), tag);
            return itemMeta.getCustomTagContainer().hasCustomTag(key, ItemTagType.STRING) ? itemMeta.getCustomTagContainer().getCustomTag(key, ItemTagType.STRING) : null;
        }

        // 1.13 R1 and lower full NMS
        return CompatibilityAPI.getCompatibility().getNBTCompatibility().getCustomTag(itemStack, tag);
    }

    /**
     * Set or change tag of item stack with given value.
     *
     * @param itemStack the item stack to modify
     * @param tag       the tag name
     * @param value     the value for tag
     * @return the item stack with new or modified tag value
     */
    public static ItemStack setStringTag(ItemStack itemStack, String tag, String value) {

        // 1.14 R1 and above PersistentDataContainer
        if (CompatibilityAPI.getVersion() >= 1.141) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), tag);
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        // 1.13 R2 CustomItemTagContainer
        if (CompatibilityAPI.getVersion() >= 1.132) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), tag);
            itemMeta.getCustomTagContainer().setCustomTag(key, ItemTagType.STRING, value);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        // 1.13 R1 and lower full NMS
        ItemStack newItemStack = CompatibilityAPI.getCompatibility().getNBTCompatibility().setCustomTag(itemStack, tag, value);
        itemStack.setItemMeta(newItemStack.getItemMeta());
        return itemStack;
    }

    /**
     * @param itemStack the item stack from which to get tag
     * @param tag       the tag name
     * @return the value of the tag (may be null)
     */
    public static Integer getIntegerTag(ItemStack itemStack, String tag) {

        // 1.14 R1 and above PersistentDataContainer
        if (CompatibilityAPI.getVersion() >= 1.141) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), tag);
            return itemMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER) ? itemMeta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER) : null;
        }

        // 1.13 R2 CustomItemTagContainer
        if (CompatibilityAPI.getVersion() >= 1.132) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), tag);
            return itemMeta.getCustomTagContainer().hasCustomTag(key, ItemTagType.INTEGER) ? itemMeta.getCustomTagContainer().getCustomTag(key, ItemTagType.INTEGER) : null;
        }

        // 1.13 R1 and lower full NMS
        // Lazy implementation for integers
        String tagValue = CompatibilityAPI.getCompatibility().getNBTCompatibility().getCustomTag(itemStack, tag);
        return tagValue == null ? null : Integer.valueOf(tagValue);
    }

    /**
     * Set or change tag of item stack with given value.
     *
     * @param itemStack the item stack to modify
     * @param tag       the tag name
     * @param value     the value for tag
     * @return the item stack with new or modified tag value
     */
    public static ItemStack setIntegerTag(ItemStack itemStack, String tag, int value) {

        // 1.14 R1 and above PersistentDataContainer
        if (CompatibilityAPI.getVersion() >= 1.141) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), tag);
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        // 1.13 R2 CustomItemTagContainer
        if (CompatibilityAPI.getVersion() >= 1.132) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), tag);
            itemMeta.getCustomTagContainer().setCustomTag(key, ItemTagType.INTEGER, value);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        // 1.13 R1 and lower full NMS
        // Lazy implementation for integers
        ItemStack newItemStack = CompatibilityAPI.getCompatibility().getNBTCompatibility().setCustomTag(itemStack, tag, String.valueOf(value));
        itemStack.setItemMeta(newItemStack.getItemMeta());
        return itemStack;
    }

    /**
     * Adds attribute for item stack with given amount.
     * This method keeps other attributes set by for example commands or other plugins, BUT
     * overrides WeaponMechanics old attribute value if found.
     *
     * @param itemStack     the item stack to which attribute will be set
     * @param attributeType the attribute type
     * @param amount        the amount for attribute type
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
        ItemStack newItemStack = CompatibilityAPI.getCompatibility().getNBTCompatibility().setAttributeValue(itemStack, attributeType, amount);
        itemStack.setItemMeta(newItemStack.getItemMeta());
        return itemStack;
    }
}
