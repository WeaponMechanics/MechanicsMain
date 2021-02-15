package me.deecaad.compatibility.nbt;

import me.deecaad.core.utils.AttributeType;
import org.bukkit.inventory.ItemStack;

/**
 * This interface outlines a version dependant api that return values based on
 * different {@link ItemStack} and tag input. There should be an implementing
 * class for each minecraft protocol version.
 *
 * TODO Replace {@link me.deecaad.core.utils.TagUtil} and use this class directly.
 */
public interface NBTCompatibility {


    String getCustomTag(ItemStack itemStack, String tag);


    String getCustomTagFromNMSStack(Object nmsStack, String tag);


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