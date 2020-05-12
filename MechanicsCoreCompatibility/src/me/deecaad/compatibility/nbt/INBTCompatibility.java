package me.deecaad.compatibility.nbt;

import me.deecaad.core.utils.AttributeType;
import org.bukkit.inventory.ItemStack;

public interface INBTCompatibility {

    /**
     * @param itemStack the item stack from which to get tag
     * @param tag the tag name
     * @return the value of the tag (may be null)
     */
    String getCustomTag(ItemStack itemStack, String tag);

    /**
     * @param nmsStack the nms stack from which to get tag
     * @param tag the tag name
     * @return the value of the tag (may be null)
     */
    String getCustomTagFromNMSStack(Object nmsStack, String tag);

    /**
     * Set or change tag of item stack with given value.
     *
     * @param itemStack the item stack to modify
     * @param tag the tag name
     * @param value the value for tag
     * @return the item stack with new or modified tag value
     */
    ItemStack setCustomTag(ItemStack itemStack, String tag, String value);

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
    ItemStack setAttributeValue(ItemStack itemStack, AttributeType attributeType, double amount);
}