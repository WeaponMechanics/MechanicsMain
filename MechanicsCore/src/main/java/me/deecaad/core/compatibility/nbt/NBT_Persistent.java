package me.deecaad.core.compatibility.nbt;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * Due to class loading issues, this logic (that was originally implemented in
 * {@link NBTCompatibility}) is now implemented here. This way, on 1.12.2,
 * {@link org.bukkit.persistence.PersistentDataType} is not loaded.
 */
public abstract class NBT_Persistent implements NBTCompatibility {

    public boolean hasString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), PersistentDataType.STRING);
    }

    public String getString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.getOrDefault(getKey(plugin, key), PersistentDataType.STRING, def);
    }

    public void setString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), PersistentDataType.STRING, value);
        bukkitItem.setItemMeta(meta);
    }

    public boolean hasInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), PersistentDataType.INTEGER);
    }

    public int getInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.getOrDefault(getKey(plugin, key), PersistentDataType.INTEGER, def);
    }

    public void setInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), PersistentDataType.INTEGER, value);
        bukkitItem.setItemMeta(meta);
    }

    public boolean hasDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), PersistentDataType.DOUBLE);
    }

    public double getDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, double def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.getOrDefault(getKey(plugin, key), PersistentDataType.DOUBLE, def);
    }

    public void setDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, double value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), PersistentDataType.DOUBLE, value);
        bukkitItem.setItemMeta(meta);
    }

    public boolean hasArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), PersistentDataType.INTEGER_ARRAY);
    }

    public int[] getArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int[] def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.has(getKey(plugin, key), PersistentDataType.INTEGER_ARRAY) ? nbt.get(getKey(plugin, key), PersistentDataType.INTEGER_ARRAY) : def;
    }

    public void setArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int[] value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), PersistentDataType.INTEGER_ARRAY, value);
        bukkitItem.setItemMeta(meta);
    }

    public boolean hasStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), StringPersistentType.INSTANCE);
    }

    public String[] getStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String[] def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.has(getKey(plugin, key), StringPersistentType.INSTANCE) ? nbt.get(getKey(plugin, key), StringPersistentType.INSTANCE) : def;
    }

    public void setStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String[] value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), StringPersistentType.INSTANCE, value);
        bukkitItem.setItemMeta(meta);
    }

    public void remove(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.remove(getKey(plugin, key));
        bukkitItem.setItemMeta(meta);
    }

    private PersistentDataContainer getCompound(@NotNull ItemMeta meta) {
        return meta.getPersistentDataContainer();
    }
}
