package me.deecaad.core.compatibility.nbt;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * This interface outlines a version dependant api that return values based on different
 * {@link ItemStack} and tag input. There should be an implementing class for each minecraft
 * protocol version.
 *
 * <p>
 * NBT stands for Named Binary Tag.
 */
public interface NBTCompatibility {

    // Used in getArray, so we don't have to instantiate a new array every call
    int[] DO_NOT_MODIFY_ME = new int[0];
    String[] DO_NOT_MODIFY_ME_STRING = new String[0];

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT compound has the given
     * <code>key</code>, and it is a {@link String} value. Otherwise, this method will return
     * <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key);

    /**
     * Returns the {@link String} value of a NBT tag with the given name <code>key</code>. The value is
     * pulled from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag isn't
     * used in the item's compound, then this method will return <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @return The value of the tag, or <code>null</code>.
     */
    default String getString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getString(bukkitItem, plugin, key, null);
    }

    /**
     * Returns the {@link String} value of a NBT tag with the given name <code>key</code>. The value is
     * pulled from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag isn't
     * used in the item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @param def The default value to return if the key is not present.
     * @return The value of the tag, or <code>null</code>.
     */
    String getString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String def);

    /**
     * Sets the {@link String} value of a NBT tag with the given name <code>key</code>. The value is put
     * in the NBT compound stored in the given item <code>bukkitItem</code>.
     *
     * <p>
     * The stored value can be seen using {@link #getString(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to store the value at.
     * @param value The value that will be stored.
     */
    void setString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String value);

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT compound has the given
     * <code>key</code>, and it is an {@link Integer} value. Otherwise, this method will return
     * <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key);

    /**
     * Returns the {@link Integer} value of a NBT tag with the given name <code>key</code>. The value is
     * pulled from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag isn't
     * used in the item's compound, then this method will return <code>0</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @return The value of the tag, or <code>0</code>.
     */
    default int getInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getInt(bukkitItem, plugin, key, 0);
    }

    /**
     * Returns the {@link Integer} value of a NBT tag with the given name <code>key</code>. The value is
     * pulled from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag isn't
     * used in the item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @param def The default value to return if the key is not present.
     * @return The value of the tag, or <code>0</code>.
     */
    int getInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int def);

    /**
     * Sets the {@link Integer} value of a NBT tag with the given name <code>key</code>. The value is
     * put in the NBT compound stored in the given item <code>bukkitItem</code>.
     *
     * <p>
     * The stored value can be seen using {@link #getInt(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to store the value at.
     * @param value The value that will be stored.
     */
    void setInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int value);

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT compound has the given
     * <code>key</code>, and it is a {@link Double} value. Otherwise, this method will return
     * <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key);

    /**
     * Returns the {@link Double} value of a NBT tag with the given name <code>key</code>. The value is
     * pulled from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag isn't
     * used in the item's compound, then this method will return <code>0.0</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @return The value of the tag, or <code>0</code>.
     */
    default double getDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getDouble(bukkitItem, plugin, key, 0.0);
    }

    /**
     * Returns the {@link Double} value of a NBT tag with the given name <code>key</code>. The value is
     * pulled from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag isn't
     * used in the item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @param def The default value to return if the key is not present.
     * @return The value of the tag, or <code>0</code>.
     */
    double getDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, double def);

    /**
     * Sets the {@link Double} value of a NBT tag with the given name <code>key</code>. The value is put
     * in a compound according to the given <code>plugin</code>, inside of the <code>bukkitItem</code>
     * NBT compound.
     *
     * <p>
     * The stored value can be seen using {@link #getDouble(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to store the value at.
     * @param value The value that will be stored.
     */
    void setDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, double value);

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT compound has the given
     * <code>key</code>, and it is an int[] value. Otherwise, this method will return <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key);

    /**
     * Returns the int[] value of a NBT tag with the given name <code>key</code>. The value is pulled
     * from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag isn't used
     * in the item's compound, then this method will return <code>0.0</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @return The value of the tag, or an empty array.
     */
    default int[] getArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getArray(bukkitItem, plugin, key, DO_NOT_MODIFY_ME);
    }

    /**
     * Returns the int[] value of a NBT tag with the given name <code>key</code>. The value is pulled
     * from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag isn't used
     * in the item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @param def The default value to return if the key is not present.
     * @return The value of the tag, or <code>def</code>.
     */
    int[] getArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int[] def);

    /**
     * Sets the int[] value of a NBT tag with the given name <code>key</code>. The value is put in a
     * compound according to the given <code>plugin</code>, inside of the <code>bukkitItem</code> NBT
     * compound.
     *
     * <p>
     * The stored value can be seen using {@link #getArray(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to store the value at.
     * @param value The value that will be stored.
     */
    void setArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int[] value);

    /**
     * Returns <code>true</code> if the given <code>bukkitItem</code>'s NBT compound has the given
     * <code>key</code>, and it is a {@link String}[] value. Otherwise, this method will return
     * <code>null</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to check existence of.
     * @return <code>true</code> if the NBT compound uses the tag.
     */
    boolean hasStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key);

    /**
     * Returns the {@link String}[] value of a NBT tag with the given name <code>key</code>. The value
     * is pulled from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag
     * isn't used in the item's compound, then this method will return <code>0.0</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @return The value of the tag, or an empty array.
     */
    default String[] getStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getStringArray(bukkitItem, plugin, key, DO_NOT_MODIFY_ME_STRING);
    }

    /**
     * Returns the {@link String}[] value of a NBT tag with the given name <code>key</code>. The value
     * is pulled from an NBT compound contained in the given item <code>bukkitItem</code>. If the tag
     * isn't used in the item's compound, then this method will return <code>def</code>.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to pull some value from.
     * @param def The default value to return if the key is not present.
     * @return The value of the tag, or <code>def</code>.
     */
    String[] getStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String[] def);

    /**
     * Sets the {@link Double} value of a NBT tag with the given name <code>key</code>. The value is put
     * in a compound according to the given <code>plugin</code>, inside of the <code>bukkitItem</code>
     * NBT compound.
     *
     * <p>
     * The stored value can be seen using {@link #getStringArray(ItemStack, String, String)}.
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to store the value at.
     * @param value The value that will be stored.
     */
    void setStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String[] value);

    /**
     * Removes the given NBT tag from the item. To check to see if there was a key to delete, use
     * {@link #hasString(ItemStack, String, String)} (or one of the <code>hasX</code> methods).
     *
     * @param bukkitItem The non-null item that has an NBT tag compound.
     * @param plugin The non-null owner of the tag, should be your plugin.
     * @param key The non-null name of the tag to remove.
     */
    void remove(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key);

    default NamespacedKey getKey(String plugin, String key) {
        return new NamespacedKey(plugin.toLowerCase(Locale.ROOT), key.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the {@link Object#toString()} value of an item's NBT compound, useful for debugging.
     *
     * @param bukkitStack The non-null bukkit item to check the nbt tags of.
     * @return The non-null string value of the nbt compound.
     */
    @NotNull String getNBTDebug(@NotNull ItemStack bukkitStack);
}