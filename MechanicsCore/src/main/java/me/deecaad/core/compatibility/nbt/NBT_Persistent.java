package me.deecaad.core.compatibility.nbt;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;

/**
 * Due to class loading issues, this logic (that was originally implemented in
 * {@link NBTCompatibility}) is now implemented here. This way, on 1.12.2,
 * {@link org.bukkit.persistence.PersistentDataType} is not loaded.
 */
public abstract class NBT_Persistent implements NBTCompatibility {

    public boolean hasString(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), PersistentDataType.STRING);
    }

    public String getString(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, String def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.getOrDefault(getKey(plugin, key), PersistentDataType.STRING, def);
    }

    public void setString(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, String value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), PersistentDataType.STRING, value);
        bukkitItem.setItemMeta(meta);
    }

    public boolean hasInt(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), PersistentDataType.INTEGER);
    }

    public int getInt(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, int def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.getOrDefault(getKey(plugin, key), PersistentDataType.INTEGER, def);
    }

    public void setInt(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, int value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), PersistentDataType.INTEGER, value);
        bukkitItem.setItemMeta(meta);
    }

    public boolean hasDouble(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), PersistentDataType.DOUBLE);
    }

    public double getDouble(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, double def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.getOrDefault(getKey(plugin, key), PersistentDataType.DOUBLE, def);
    }

    public void setDouble(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, double value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), PersistentDataType.DOUBLE, value);
        bukkitItem.setItemMeta(meta);
    }

    public boolean hasArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), PersistentDataType.INTEGER_ARRAY);
    }

    public int[] getArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, int[] def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.has(getKey(plugin, key), PersistentDataType.INTEGER_ARRAY) ? nbt.get(getKey(plugin, key), PersistentDataType.INTEGER_ARRAY) : def;
    }

    public void setArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, int[] value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), PersistentDataType.INTEGER_ARRAY, value);
        bukkitItem.setItemMeta(meta);
    }

    public boolean hasStringArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        return getCompound(bukkitItem.getItemMeta()).has(getKey(plugin, key), StringPersistentType.INSTANCE);
    }

    public String[] getStringArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, String[] def) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        return nbt.has(getKey(plugin, key), StringPersistentType.INSTANCE) ? nbt.get(getKey(plugin, key), StringPersistentType.INSTANCE) : def;
    }

    public void setStringArray(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key, String[] value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.set(getKey(plugin, key), StringPersistentType.INSTANCE, value);
        bukkitItem.setItemMeta(meta);
    }

    public void remove(@Nonnull ItemStack bukkitItem, @Nonnull String plugin, @Nonnull String key) {
        ItemMeta meta = bukkitItem.getItemMeta();
        PersistentDataContainer nbt = getCompound(meta);

        nbt.remove(getKey(plugin, key));
        bukkitItem.setItemMeta(meta);
    }

    private PersistentDataContainer getCompound(@Nonnull ItemMeta meta) {
        return meta.getPersistentDataContainer();
    }
}
