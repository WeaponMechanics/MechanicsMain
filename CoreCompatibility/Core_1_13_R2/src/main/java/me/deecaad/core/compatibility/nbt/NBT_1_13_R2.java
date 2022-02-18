package me.deecaad.core.compatibility.nbt;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import net.minecraft.server.v1_13_R2.NBTBase;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftCustomTagTypeRegistry;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R2.inventory.tags.CraftCustomItemTagContainer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public boolean hasString(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key) {
        return getBukkitCompound(bukkitItem.getItemMeta(), plugin).hasCustomTag(getKey(key), ItemTagType.STRING);
    }

    @Override
    public String getString(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, String def) {
        CustomItemTagContainer nbt = getBukkitCompound(bukkitItem.getItemMeta(), plugin);
        NamespacedKey tag = getKey(key);

        return nbt.hasCustomTag(tag, ItemTagType.STRING) ? nbt.getCustomTag(tag, ItemTagType.STRING) : def;
    }

    @Override
    public void setString(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, String value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        CustomItemTagContainer nbt = getBukkitCompound(meta, plugin);

        nbt.setCustomTag(getKey(key), ItemTagType.STRING, value);
        bukkitItem.setItemMeta(meta);
    }

    @Override
    public boolean hasInt(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key) {
        return getBukkitCompound(bukkitItem.getItemMeta(), plugin).hasCustomTag(getKey(key), ItemTagType.INTEGER);
    }

    @Override
    public int getInt(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, int def) {
        CustomItemTagContainer nbt = getBukkitCompound(bukkitItem.getItemMeta(), plugin);
        NamespacedKey tag = getKey(key);

        return nbt.hasCustomTag(tag, ItemTagType.INTEGER) ? nbt.getCustomTag(tag, ItemTagType.INTEGER) : def;
    }

    @Override
    public void setInt(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, int value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        CustomItemTagContainer nbt = getBukkitCompound(meta, plugin);

        nbt.setCustomTag(getKey(key), ItemTagType.INTEGER, value);
        bukkitItem.setItemMeta(meta);
    }

    @Override
    public boolean hasDouble(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key) {
        return getBukkitCompound(bukkitItem.getItemMeta(), plugin).hasCustomTag(getKey(key), ItemTagType.DOUBLE);
    }

    @Override
    public double getDouble(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, double def) {
        CustomItemTagContainer nbt = getBukkitCompound(bukkitItem.getItemMeta(), plugin);
        NamespacedKey tag = getKey(key);

        return nbt.hasCustomTag(tag, ItemTagType.DOUBLE) ? nbt.getCustomTag(tag, ItemTagType.DOUBLE) : def;
    }

    @Override
    public void setDouble(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, double value) {
        ItemMeta meta = bukkitItem.getItemMeta();
        CustomItemTagContainer nbt = getBukkitCompound(meta, plugin);

        nbt.setCustomTag(getKey(key), ItemTagType.DOUBLE, value);
        bukkitItem.setItemMeta(meta);
    }

    @Nonnull
    @Override
    public net.minecraft.server.v1_13_R2.ItemStack getNMSStack(@Nonnull ItemStack bukkitStack) {
        return CraftItemStack.asNMSCopy(bukkitStack);
    }

    @Nonnull
    @Override
    public ItemStack getBukkitStack(@Nonnull Object nmsStack) {
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_13_R2.ItemStack) nmsStack);
    }

    @Nonnull
    @Override
    public String getNBTDebug(@Nonnull ItemStack bukkitStack) {
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

    private CustomItemTagContainer getBukkitCompound(ItemMeta meta, String plugin) {
        if (plugin == null) {
            meta.getCustomTagContainer();
        }

        NamespacedKey key = new NamespacedKey(MechanicsCore.getPlugin(), plugin);
        CustomItemTagContainer nbt = meta.getCustomTagContainer().getCustomTag(key, ItemTagType.TAG_CONTAINER);

        if (nbt == null) {
            nbt = new CraftCustomItemTagContainer(new CraftCustomTagTypeRegistry());
            meta.getCustomTagContainer().setCustomTag(key, ItemTagType.TAG_CONTAINER, nbt);
        }

        return nbt;
    }

    private NamespacedKey getKey(String key) {
        return new NamespacedKey(MechanicsCore.getPlugin(), key);
    }
}
