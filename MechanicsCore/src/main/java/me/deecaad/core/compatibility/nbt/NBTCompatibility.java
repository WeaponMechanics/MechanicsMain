package me.deecaad.core.compatibility.nbt;

import me.deecaad.core.utils.AttributeType;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.UUID;

/**
 * This interface outlines a version dependant api that return values based on
 * different {@link ItemStack} and tag input. There should be an implementing
 * class for each minecraft protocol version.
 *
 * <p>NBT stands for Named Binary Tag.
 */
public interface NBTCompatibility {

    // Used in getArray, so we don't have to instantiate a new array every call
    int[] DO_NOT_MODIFY_ME = new int[0];
    String[] DO_NOT_MODIFY_ME_STRING = new String[0];

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT
     * compound has the given <code>key</code>, and it is a {@link String}
     * value. Otherwise, this method will return <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasString(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key);

    /**
     * Returns the {@link String} value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @return The value of the tag, or <code>null</code>.
     */
    default String getString(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getString(bukkitItem, plugin, key, null);
    }

    /**
     * Returns the {@link String} value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @param def        The default value to return if the key is not present.
     * @return The value of the tag, or <code>null</code>.
     */
    String getString(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, String def);

    /**
     * Sets the {@link String} value of a NBT tag with the given name
     * <code>key</code>. The value is put in the NBT compound stored in the
     * given item <code>bukkitItem</code>.
     *
     * <p>The stored value can be seen using
     * {@link #getString(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to store the value at.
     * @param value      The value that will be stored.
     */
    void setString(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, String value);

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT
     * compound has the given <code>key</code>, and it is an {@link Integer}
     * value. Otherwise, this method will return <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasInt(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key);

    /**
     * Returns the {@link Integer} value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>0</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @return The value of the tag, or <code>0</code>.
     */
    default int getInt(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getInt(bukkitItem, plugin, key, 0);
    }

    /**
     * Returns the {@link Integer} value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @param def        The default value to return if the key is not present.
     * @return The value of the tag, or <code>0</code>.
     */
    int getInt(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, int def);

    /**
     * Sets the {@link Integer} value of a NBT tag with the given name
     * <code>key</code>. The value is put in the NBT compound stored in the
     * given item <code>bukkitItem</code>.
     *
     * <p>The stored value can be seen using
     * {@link #getInt(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to store the value at.
     * @param value      The value that will be stored.
     */
    void setInt(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, int value);

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT
     * compound has the given <code>key</code>, and it is a {@link Double}
     * value. Otherwise, this method will return <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasDouble(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key);

    /**
     * Returns the {@link Double} value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>0.0</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @return The value of the tag, or <code>0</code>.
     */
    default double getDouble(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getDouble(bukkitItem, plugin, key, 0.0);
    }

    /**
     * Returns the {@link Double} value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @param def        The default value to return if the key is not present.
     * @return The value of the tag, or <code>0</code>.
     */
    double getDouble(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, double def);

    /**
     * Sets the {@link Double} value of a NBT tag with the given name
     * <code>key</code>. The value is put in a compound according to the given
     * <code>plugin</code>, inside of the <code>bukkitItem</code> NBT compound.
     *
     * <p>The stored value can be seen using
     * {@link #getDouble(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to store the value at.
     * @param value      The value that will be stored.
     */
    void setDouble(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, double value);

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT
     * compound has the given <code>key</code>, and it is an int[]
     * value. Otherwise, this method will return <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key);

    /**
     * Returns the int[] value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>0.0</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @return The value of the tag, or an empty array.
     */
    default int[] getArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getArray(bukkitItem, plugin, key, DO_NOT_MODIFY_ME);
    }

    /**
     * Returns the int[] value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @param def        The default value to return if the key is not present.
     * @return The value of the tag, or <code>def</code>.
     */
    int[] getArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, int[] def);

    /**
     * Sets the int[] value of a NBT tag with the given name
     * <code>key</code>. The value is put in a compound according to the given
     * <code>plugin</code>, inside of the <code>bukkitItem</code> NBT compound.
     *
     * <p>The stored value can be seen using
     * {@link #getArray(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to store the value at.
     * @param value      The value that will be stored.
     */
    void setArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, int[] value);

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT
     * compound has the given <code>key</code>, and it is a {@link String}[]
     * value. Otherwise, this method will return <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasStringArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key);

    /**
     * Returns the {@link String}[] value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>0.0</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @return The value of the tag, or an empty array.
     */
    default String[] getStringArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getStringArray(bukkitItem, plugin, key, DO_NOT_MODIFY_ME_STRING);
    }

    /**
     * Returns the {@link String}[] value of a NBT tag with the given name
     * <code>key</code>. The value is pulled from an NBT compound contained in
     * the given item <code>bukkitItem</code>. If the tag isn't used in the
     * item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to pull some value from.
     * @param def        The default value to return if the key is not present.
     * @return The value of the tag, or <code>def</code>.
     */
    String[] getStringArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, String[] def);

    /**
     * Sets the {@link Double} value of a NBT tag with the given name
     * <code>key</code>. The value is put in a compound according to the given
     * <code>plugin</code>, inside of the <code>bukkitItem</code> NBT compound.
     *
     * <p>The stored value can be seen using
     * {@link #getStringArray(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to store the value at.
     * @param value      The value that will be stored.
     */
    void setStringArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, String[] value);

    /**
     * Removes the given NBT tag from the item. To check to see if there was
     * a key to delete, use {@link #hasString(ItemStack, String, String)} (or
     * one of the <code>hasX</code> methods).
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin     The non-null owner of the tag, should be your plugin.
     * @param key        The non-null name of the tag to remove.
     */
    void remove(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key);

    /**
     * Sets the value of an {@link Attribute} for a given item
     * <code>bukkitItem</code>. The attribute will apply to living entities
     * which hold the item in the given <code>slot</code>. Using this method
     * overrides the value previously set, if applicable.
     *
     * <p>This method always adds the attribute value (No multiplication).
     *
     * @param bukkitItem The non-null item to apply the attribute to.
     * @param attribute  The non-null attribute to set.
     * @param slot       In which slot will the attribute be active, or
     *                   <code>null</code> for all slots.
     * @param value      The new value of the attribute.
     */
    default void setAttribute(@Nonnull ItemStack bukkitItem, @Nonnull AttributeType attribute, @Nullable AttributeSlot slot, double value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        Attribute bukkitAttribute = Attribute.valueOf(attribute.name());

        AttributeModifier hand = new AttributeModifier(slot == null ? attribute.getUUID() : slot.modify(attribute.getUUID()), "MechanicsCoreAttribute", value, AttributeModifier.Operation.ADD_NUMBER, slot == null ? null : slot.getEquipmentSlot());

        // API doesn't allow modifying AttributeModifiers so I have to delete old ones based on their UUIDs
        // and add these new AttributeModifiers which contain new amount for the Attribute
        meta.removeAttributeModifier(bukkitAttribute, hand);
        meta.addAttributeModifier(bukkitAttribute, hand);

        bukkitItem.setItemMeta(meta);
    }

    default NamespacedKey getKey(String plugin, String key) {
        return new NamespacedKey(plugin.toLowerCase(), key.toLowerCase());
    }

    /**
     * Copies the NBT tags from item to the other. This method will replace the
     * previous NBT tags of the item. A <i>COPY</i> of the tags are used, so
     * any modifications to one of the item's tags will not be reflected in the
     * other after this method is used.
     *
     * @param fromItem The non-null item to copy the tags from.
     * @param toItem   The non-null item to override the tags.
     * @param path     The path to the compound to copy, or null to copy every
     *                 tag. Example: <code>"PublicBukkitValues"</code>
     */
    void copyTagsFromTo(@Nonnull ItemStack fromItem, @Nonnull ItemStack toItem, String path);

    /**
     * Returns a NMS item stack based on the given <code>bukkitStack</code>.
     *
     * @param bukkitStack The non-null bukkit item to convert.
     * @return The non-null nms item.
     */
    @Nonnull
    Object getNMSStack(@Nonnull ItemStack bukkitStack);

    /**
     * Returns a bukkit item stack based on the given <code>nmsStack</code>.
     *
     * @param nmsStack The non-null nms item to convert.
     * @return The non-null bukkit item.
     */
    @Nonnull
    ItemStack getBukkitStack(@Nonnull Object nmsStack);

    /**
     * Returns the {@link Object#toString()} value of an item's NBT compound,
     * useful for debugging.
     *
     * @param bukkitStack The non-null bukkit item to check the nbt tags of.
     * @return The non-null string value of the nbt compound.
     */
    @Nonnull
    String getNBTDebug(@Nonnull ItemStack bukkitStack);

    /**
     * This enum outlines the different slots an attribute can be applied to.
     */
    enum AttributeSlot {

        MAIN_HAND(1),
        OFF_HAND(6),
        FEET(5),
        LEGS(4),
        CHEST(3),
        HEAD(2);

        private final EquipmentSlot slot;
        private final String slotName;
        private final long uuidModifier;

        AttributeSlot(long uuidModifier) {
            this.slot = name().equals("MAIN_HAND") ? EquipmentSlot.HAND : EquipmentSlot.valueOf(name());
            this.slotName = name().replaceAll("_", "").toLowerCase(Locale.ROOT);
            this.uuidModifier = uuidModifier;
        }

        public EquipmentSlot getEquipmentSlot() {
            return slot;
        }

        public String getSlotName() {
            return slotName;
        }

        public UUID modify(UUID uuid) {
            return new UUID(uuid.getMostSignificantBits() + uuidModifier, uuid.getLeastSignificantBits());
        }

        @Override
        public String toString() {
            return "AttributeSlot{" +
                    "slot=" + slot +
                    ", slotName='" + slotName + '\'' +
                    '}';
        }
    }
}