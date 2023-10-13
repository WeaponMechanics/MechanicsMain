package me.deecaad.core.compatibility.nbt;

import me.deecaad.core.utils.AttributeType;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NBT_1_12_R1 implements NBTCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 12) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + NBT_1_12_R1.class + " when not using Minecraft 12",
                    new InternalError()
            );
        }
    }

    @Override
    public void copyTagsFromTo(@NotNull ItemStack fromItem, @NotNull ItemStack toItem, @Nullable String path) {
        net.minecraft.server.v1_12_R1.ItemStack nms = getNMSStack(toItem);
        NBTTagCompound from = getNMSStack(fromItem).getTag();
        NBTTagCompound to = nms.getTag();

        if (path == null) {
            nms.setTag((NBTTagCompound) from.clone());
            toItem.setItemMeta(CraftItemStack.asBukkitCopy(nms).getItemMeta());
            return;
        }

        to.set(path, from.getCompound(path).clone());
        toItem.setItemMeta(CraftItemStack.asBukkitCopy(nms).getItemMeta());
    }

    @Override
    public boolean hasString(@NotNull ItemStack bukkitItem, @Nullable String plugin, @NotNull String key) {
        return getBukkitCompound(getNMSStack(bukkitItem)).hasKey(getTagName(plugin, key));
    }

    @Override
    public String getString(@NotNull ItemStack bukkitItem, @Nullable String plugin, @NotNull String key, String def) {
        String value = getBukkitCompound(getNMSStack(bukkitItem)).getString(getTagName(plugin, key));
        return value != null && !value.isEmpty() ? value : def;
    }

    @NotNull
    @Override
    public void setString(@NotNull ItemStack bukkitItem, @Nullable String plugin, @NotNull String key, String value) {
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = getNMSStack(bukkitItem);
        getBukkitCompound(nmsStack).setString(getTagName(plugin, key), value);

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
    }

    @Override
    public boolean hasInt(@NotNull ItemStack bukkitItem, @Nullable String plugin, @NotNull String key) {
        return getBukkitCompound(getNMSStack(bukkitItem)).hasKey(getTagName(plugin, key));
    }

    @Override
    public int getInt(@NotNull ItemStack bukkitItem, @Nullable String plugin, @NotNull String key, int def) {
        NBTTagCompound nbt = getBukkitCompound(getNMSStack(bukkitItem));
        String tag = getTagName(plugin, key);
        if (!nbt.hasKey(tag)) return def;
        return nbt.getInt(tag);
    }

    @NotNull
    @Override
    public void setInt(@NotNull ItemStack bukkitItem, @Nullable String plugin, @NotNull String key, int value) {
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = getNMSStack(bukkitItem);
        getBukkitCompound(nmsStack).setInt(getTagName(plugin, key), value);

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
    }

    @Override
    public boolean hasDouble(@NotNull ItemStack bukkitItem, @Nullable String plugin, @NotNull String key) {
        return getBukkitCompound(getNMSStack(bukkitItem)).hasKey(getTagName(plugin, key));
    }

    @Override
    public double getDouble(@NotNull ItemStack bukkitItem, @Nullable String plugin, @NotNull String key, double def) {
        NBTTagCompound nbt = getBukkitCompound(getNMSStack(bukkitItem));
        String tag = getTagName(plugin, key);
        if (!nbt.hasKey(tag)) return def;
        return nbt.getDouble(tag);
    }

    @Override
    public void setDouble(@NotNull ItemStack bukkitItem, @Nullable String plugin, @NotNull String key, double value) {
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = getNMSStack(bukkitItem);
        getBukkitCompound(nmsStack).setDouble(getTagName(plugin, key), value);

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
    }

    @Override
    public boolean hasArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getBukkitCompound(getNMSStack(bukkitItem)).hasKey(getTagName(plugin, key));
    }

    @Override
    public int[] getArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int[] def) {
        NBTTagCompound nbt = getBukkitCompound(getNMSStack(bukkitItem));
        String tag = getTagName(plugin, key);
        if (!nbt.hasKey(tag)) return def;
        return nbt.getIntArray(tag);
    }

    @Override
    public void setArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, int[] value) {
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = getNMSStack(bukkitItem);
        getBukkitCompound(nmsStack).setIntArray(getTagName(plugin, key), value);

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
    }

    @Override
    public boolean hasStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        return getBukkitCompound(getNMSStack(bukkitItem)).hasKey(getTagName(plugin, key));
    }

    @Override
    public String[] getStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String[] def) {
        NBTTagCompound nbt = getBukkitCompound(getNMSStack(bukkitItem));
        String tag = getTagName(plugin, key);
        if (!nbt.hasKey(tag)) return def;

        byte[] primitive = nbt.getByteArray(tag);
        final ByteBuffer buffer = ByteBuffer.wrap(primitive);
        final List<String> list = new ArrayList<>();

        while (buffer.remaining() > 0) {
            if (buffer.remaining() < 4) break;
            final int stringLength = buffer.getInt();
            if (buffer.remaining() < stringLength) break;

            final byte[] stringBytes = new byte[stringLength];
            buffer.get(stringBytes);

            list.add(new String(stringBytes, StandardCharsets.UTF_8));
        }

        return list.toArray(new String[0]);
    }

    @Override
    public void setStringArray(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key, String[] complex) {
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = getNMSStack(bukkitItem);

        final byte[][] allBytes = new byte[complex.length][];
        int total = 0;
        for (int i = 0; i < allBytes.length; i++) {
            final byte[] bytes = complex[i].getBytes(StandardCharsets.UTF_8);
            allBytes[i] = bytes;
            total += bytes.length;
        }

        final ByteBuffer buffer = ByteBuffer.allocate(total + allBytes.length * 4); //stores integers
        for (final byte[] bytes : allBytes) {
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }

        getBukkitCompound(nmsStack).setByteArray(getTagName(plugin, key), buffer.array());

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
    }


    @Override
    public void remove(@NotNull ItemStack bukkitItem, @NotNull String plugin, @NotNull String key) {
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = getNMSStack(bukkitItem);
        getBukkitCompound(nmsStack).remove(getTagName(plugin, key));

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
    }

    @Override
    public void setAttribute(@NotNull ItemStack bukkitItem, @NotNull AttributeType attribute, @Nullable AttributeSlot slot, double value) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = getNMSStack(bukkitItem);
        if (nmsItem.getTag() == null) {
            nmsItem.setTag(new NBTTagCompound());
        }

        NBTTagCompound compound = nmsItem.getTag();
        if (!compound.hasKey("AttributeModifiers")) {
            compound.set("AttributeModifiers", new NBTTagList());
        }

        NBTTagList list = (NBTTagList) compound.get("AttributeModifiers");

        // Attributes have a UUID which is used by Minecraft... If we have
        // a duplicate UUID, an undefined attribute will be ignored
        UUID uuid = slot == null ? attribute.getUUID() : slot.modify(attribute.getUUID());

        // NBT lists don't have an indexOf method, so we need to loop
        // through each attribute, and determine if it is one we want to
        // modify. We want to modify an attribute if the attribute was
        // set using MechanicsCore, and it's attribute type matches the
        // parameter attribute type.
        boolean isModifiedAttribute = false;
        for (int i = 0; i < list.size(); i++) {

            // 10 is the id for nbt lists
            if (list.get(i).getTypeId() != 10) {
                continue;
            }

            NBTTagCompound nbt = list.get(i);
            String name = nbt.getString("Name");
            String attributeName = nbt.getString("AttributeName");
            long uuidLeast = nbt.getLong("UUIDLeast");
            long uuidMost = nbt.getLong("UUIDMost");

            if (!"MechanicsCoreAttribute".equals(name)
                    || !attribute.getMinecraftName().equals(attributeName)
                    || uuid.getLeastSignificantBits() != uuidLeast
                    || uuid.getMostSignificantBits() != uuidMost) {
                continue;
            }

            // Since this attribute already exists, we only need to modify
            // the existing value. No need to set the name/uuid
            nbt.setDouble("Amount", value);
            isModifiedAttribute = true;
            break;
        }

        if (!isModifiedAttribute) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("AttributeName", attribute.getMinecraftName());
            nbt.setString("Name", "MechanicsCoreAttribute");
            nbt.setDouble("Amount", value);
            nbt.setInt("Operation", 0); // 0 == add

            nbt.setLong("UUIDLeast", uuid.getLeastSignificantBits());
            nbt.setLong("UUIDMost", uuid.getMostSignificantBits());

            if (slot != null) {
                nbt.setString("Slot", slot.getSlotName());
            }
            list.add(nbt);
        }

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsItem));
    }

    @NotNull
    @Override
    public net.minecraft.server.v1_12_R1.ItemStack getNMSStack(@NotNull ItemStack bukkitStack) {
        return CraftItemStack.asNMSCopy(bukkitStack);
    }

    @NotNull
    @Override
    public ItemStack getBukkitStack(@NotNull Object nmsStack) {
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_12_R1.ItemStack) nmsStack);
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

        List<String> keys = new ArrayList<>(nbt.c());
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

    private NBTTagCompound getBukkitCompound(net.minecraft.server.v1_12_R1.ItemStack nmsStack) {
        if (nmsStack.getTag() == null) {
            nmsStack.setTag(new NBTTagCompound());
        }

        NBTTagCompound nbt = nmsStack.getTag().getCompound("PublicBukkitValues");

        // If the nbt compound was just created, make sure it is added to the
        // internal map.
        if (nbt.isEmpty()) {
            nmsStack.getTag().set("PublicBukkitValues", nbt);
        }
        return nbt;
    }

    private String getTagName(String plugin, String key) {
        return plugin.toLowerCase(Locale.ROOT) + ":" + key.toLowerCase(Locale.ROOT);
    }
}