package me.deecaad.weaponmechanics.utils;

import me.deecaad.core.utils.TagUtils;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.inventory.ItemStack;

public class TagHelper {

    /**
     * Don't let anyone instantiate this class
     */
    private TagHelper() { }

    /**
     * @param itemStack the item stack from which to get tag
     * @param tag the tag name
     * @return the value of the tag, if not found null
     */
    public static String getStringTag(ItemStack itemStack, CustomTag tag) {
        return TagUtils.getStringTag(itemStack, tag.getId());
    }

    /**
     * Set or change tag of item stack with given value.
     *
     * @param itemStack the item stack to modify
     * @param tag the tag name
     * @param value the value for tag
     * @return the item stack with new or modified tag value
     */
    public static ItemStack setStringTag(ItemStack itemStack, CustomTag tag, String value) {
        return setStringTag(itemStack, tag, value, null, false);
    }

    /**
     * Set or change tag of item stack with given value.
     *
     * @param itemStack the item stack to modify
     * @param tag the tag name
     * @param value the value for tag
     * @param playerWrapper the player for who set, only required if silently is true
     * @param silently if true, then next set slot packet is canclled for the given player
     * @return the item stack with new or modified tag value
     */
    public static ItemStack setStringTag(ItemStack itemStack, CustomTag tag, String value, @Nullable IPlayerWrapper playerWrapper, boolean silently) {
        if (silently && playerWrapper != null) {
            playerWrapper.setDenyNextSetSlotPacket(true);
        }
        return TagUtils.setStringTag(itemStack, tag.getId(), value);
    }

    /**
     * @param itemStack the item stack from which to get tag
     * @param tag the tag name
     * @return the value of the tag, if not found null
     */
    public static Integer getIntegerTag(ItemStack itemStack, CustomTag tag) {
        return TagUtils.getIntegerTag(itemStack, tag.getId());
    }

    /**
     * Set or change tag of item stack with given value.
     *
     * @param itemStack the item stack to modify
     * @param tag the tag name
     * @param value the value for tag
     * @return the item stack with new or modified tag value
     */
    public static ItemStack setIntegerTag(ItemStack itemStack, CustomTag tag, int value) {
        return setIntegerTag(itemStack, tag, value, null, false);
    }

    /**
     * Set or change tag of item stack with given value.
     *
     * @param itemStack the item stack to modify
     * @param tag the tag name
     * @param value the value for tag
     * @param playerWrapper the player for who set, only required if silently is true
     * @param silently if true, then next set slot packet is canclled for the given player
     * @return the item stack with new or modified tag value
     */
    public static ItemStack setIntegerTag(ItemStack itemStack, CustomTag tag, int value, @Nullable IPlayerWrapper playerWrapper, boolean silently) {
        if (silently && playerWrapper != null) {
            playerWrapper.setDenyNextSetSlotPacket(true);
        }
        return TagUtils.setIntegerTag(itemStack, tag.getId(), value);
    }
}