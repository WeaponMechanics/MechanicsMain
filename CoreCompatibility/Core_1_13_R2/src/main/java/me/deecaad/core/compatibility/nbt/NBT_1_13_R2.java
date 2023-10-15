package me.deecaad.core.compatibility.nbt;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import net.minecraft.server.v1_13_R2.NBTBase;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class NBT_1_13_R2 implements NBTCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 13) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + NBT_1_13_R2.class + " when not using Minecraft 13",
                    new InternalError()
            );
        }
    }

    @Override
    public void copyTagsFromTo(@NotNull ItemStack fromItem, @NotNull ItemStack toItem, @Nullable String path) {
        net.minecraft.server.v1_13_R2.ItemStack nms = getNMSStack(toItem);
        NBTTagCompound from = getNMSStack(fromItem).getTag();
        NBTTagCompound to = nms.getTag();

        if (path == null) {
            nms.setTag(from.clone());
            toItem.setItemMeta(CraftItemStack.asBukkitCopy(nms).getItemMeta());
            return;
        }

        to.set(path, from.getCompound(path).clone());
        toItem.setItemMeta(CraftItemStack.asBukkitCopy(nms).getItemMeta());
    }

    @Override
    public boolean hasString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getBukkitCompound(bukkitItem.getItemMeta()).hasCustomTag(getKey(plugin, key), ItemTagType.STRING);
    }

    @Override
    public String getString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String def) {
        CustomItemTagContainer nbt = getBukkitCompound(bukkitItem.getItemMeta());
        NamespacedKey tag = getKey(plugin, key);

        return nbt.hasCustomTag(tag, ItemTagType.STRING) ? nbt.getCustomTag(tag, ItemTagType.STRING) : def;
    }

    @Override
    public void setString(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        CustomItemTagContainer nbt = getBukkitCompound(meta);

        nbt.setCustomTag(getKey(plugin, key), ItemTagType.STRING, value);
        bukkitItem.setItemMeta(meta);
    }

    @Override
    public boolean hasInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getBukkitCompound(bukkitItem.getItemMeta()).hasCustomTag(getKey(plugin, key), ItemTagType.INTEGER);
    }

    @Override
    public int getInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int def) {
        CustomItemTagContainer nbt = getBukkitCompound(bukkitItem.getItemMeta());
        NamespacedKey tag = getKey(plugin, key);

        return nbt.hasCustomTag(tag, ItemTagType.INTEGER) ? nbt.getCustomTag(tag, ItemTagType.INTEGER) : def;
    }

    @Override
    public void setInt(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        CustomItemTagContainer nbt = getBukkitCompound(meta);

        nbt.setCustomTag(getKey(plugin, key), ItemTagType.INTEGER, value);
        bukkitItem.setItemMeta(meta);
    }

    @Override
    public boolean hasDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getBukkitCompound(bukkitItem.getItemMeta()).hasCustomTag(getKey(plugin, key), ItemTagType.DOUBLE);
    }

    @Override
    public double getDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, double def) {
        CustomItemTagContainer nbt = getBukkitCompound(bukkitItem.getItemMeta());
        NamespacedKey tag = getKey(plugin, key);

        return nbt.hasCustomTag(tag, ItemTagType.DOUBLE) ? nbt.getCustomTag(tag, ItemTagType.DOUBLE) : def;
    }

    @Override
    public void setDouble(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, double value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        CustomItemTagContainer nbt = getBukkitCompound(meta);

        nbt.setCustomTag(getKey(plugin, key), ItemTagType.DOUBLE, value);
        bukkitItem.setItemMeta(meta);
    }

    @Override
    public boolean hasArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getBukkitCompound(bukkitItem.getItemMeta()).hasCustomTag(getKey(plugin, key), ItemTagType.INTEGER_ARRAY);
    }

    @Override
    public int[] getArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int[] def) {
        CustomItemTagContainer nbt = getBukkitCompound(bukkitItem.getItemMeta());
        NamespacedKey tag = getKey(plugin, key);

        return nbt.hasCustomTag(tag, ItemTagType.INTEGER_ARRAY) ? nbt.getCustomTag(tag, ItemTagType.INTEGER_ARRAY) : def;
    }

    @Override
    public void setArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int[] value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        CustomItemTagContainer nbt = getBukkitCompound(meta);

        nbt.setCustomTag(getKey(plugin, key), ItemTagType.INTEGER_ARRAY, value);
        bukkitItem.setItemMeta(meta);
    }

    @Override
    public boolean hasStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getBukkitCompound(bukkitItem.getItemMeta()).hasCustomTag(getKey(plugin, key), StringTagType.INSTANCE);
    }

    @Override
    public String[] getStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String[] def) {
        CustomItemTagContainer nbt = getBukkitCompound(bukkitItem.getItemMeta());
        NamespacedKey tag = getKey(plugin, key);

        return nbt.hasCustomTag(tag, StringTagType.INSTANCE) ? nbt.getCustomTag(tag, StringTagType.INSTANCE) : def;
    }

    @Override
    public void setStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String[] value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        CustomItemTagContainer nbt = getBukkitCompound(meta);

        nbt.setCustomTag(getKey(plugin, key), StringTagType.INSTANCE, value);
        bukkitItem.setItemMeta(meta);
    }

    @Override
    public void remove(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        ItemMeta meta = bukkitItem.getItemMeta();
        CustomItemTagContainer nbt = getBukkitCompound(meta);

        nbt.removeCustomTag(getKey(plugin, key));
    }

    @NotNull
    @Override
    public net.minecraft.server.v1_13_R2.ItemStack getNMSStack(@NotNull ItemStack bukkitStack) {
        return CraftItemStack.asNMSCopy(bukkitStack);
    }

    @NotNull
    @Override
    public ItemStack getBukkitStack(@NotNull Object nmsStack) {
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_13_R2.ItemStack) nmsStack);
    }

    @NotNull
    @Override
    public String getNBTDebug(@NotNull ItemStack bukkitStack) {
        NBTTagCompound nbt = getNMSStack(bukkitStack).getTag();
        if (nbt == null)
            return "null";

        return visit(nbt, 0, 0).toString();
    }

    private static final String BRACE_COLORS = "f780"; // grayscale colors
    private static final String VALUE_COLORS = "6abcdef"; // bright colors

    private StringBuilder visit(NBTTagCompound nbt, int indents, int colorOffset) {
        String braceColor = "&" + BRACE_COLORS.charAt(indents % BRACE_COLORS.length());
        StringBuilder builder = new StringBuilder(braceColor).append('{');

        List<String> keys = new ArrayList<>(nbt.getKeys());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            NBTBase value = Objects.requireNonNull(nbt.get(key), "This is impossible");

            if (i != 0)
                builder.append('\n');

            builder.append(StringUtil.repeat("  ", indents));
            String color = "&" + VALUE_COLORS.charAt((i + colorOffset) % VALUE_COLORS.length());
            builder.append(color).append(key).append("&f&l: ").append(color);

            if (value instanceof NBTTagCompound)
                builder.append(visit((NBTTagCompound) value, indents + 1, colorOffset + i));
            else
                builder.append(value);
        }

        return builder.append(braceColor).append("}\n");
    }

    private CustomItemTagContainer getBukkitCompound(ItemMeta meta) {
        return meta.getCustomTagContainer();
    }
}
