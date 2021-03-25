package me.deecaad.compatibility.nbt;

import me.deecaad.core.utils.AttributeType;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NBT_1_8_R3 implements NBTCompatibility {

    @Override
    public boolean hasString(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key) {
        return getBukkitCompound(getNMSStack(bukkitItem), plugin).hasKeyOfType(getTagName(key), 8);
    }

    @Override
    public String getString(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, String def) {
        return hasString(bukkitItem, plugin, key) ? getBukkitCompound(getNMSStack(bukkitItem), plugin).getString(getTagName(key)) : def;
    }

    @Nonnull
    @Override
    public void setString(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, String value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = getNMSStack(bukkitItem);
        getBukkitCompound(getNMSStack(bukkitItem), plugin).setString(key, value);

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
    }

    @Override
    public boolean hasInt(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key) {
        return getBukkitCompound(getNMSStack(bukkitItem), plugin).hasKeyOfType(getTagName(key), 3);
    }

    @Override
    public int getInt(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, int def) {
        return hasInt(bukkitItem, plugin, key) ? getBukkitCompound(getNMSStack(bukkitItem), plugin).getInt(getTagName(key)) : def;
    }

    @Nonnull
    @Override
    public void setInt(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, int value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = getNMSStack(bukkitItem);
        getBukkitCompound(getNMSStack(bukkitItem), plugin).setInt(key, value);

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
    }

    @Override
    public boolean hasDouble(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key) {
        return getBukkitCompound(getNMSStack(bukkitItem), plugin).hasKeyOfType(getTagName(key), 6);
    }

    @Override
    public double getDouble(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, double def) {
        return hasDouble(bukkitItem, plugin, key) ? getBukkitCompound(getNMSStack(bukkitItem), plugin).getDouble(getTagName(key)) : def;
    }

    @Nonnull
    @Override
    public void setDouble(@Nonnull ItemStack bukkitItem, @Nullable String plugin, @Nonnull String key, double value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = getNMSStack(bukkitItem);
        getBukkitCompound(nmsStack, plugin).setDouble(key, value);

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
    }

    @Nonnull
    @Override
    public void setAttribute(@Nonnull ItemStack bukkitItem, @Nonnull AttributeType attribute, @Nullable AttributeSlot slot, double value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = getNMSStack(bukkitItem);
        if (nmsItem.getTag() == null) {
            nmsItem.setTag(new NBTTagCompound());
        }

        NBTTagCompound compound = nmsItem.getTag();
        if (compound.hasKey("AttributeModifiers")) {
            NBTTagList list = (NBTTagList) compound.get("AttributeModifiers");

            // NBT lists don't have an indexOf method, so we need to loop
            // through each attribute, and determine if it is one we want to
            // modify. We want to modify an attribute if the attribute was
            // set using MechanicsCore, and it's attribute type matches the
            // parameter attribute type.
            boolean isModifiedAttribute = false;
            for (int i = 0; i < list.size(); i++) {

                // 10 is the id for nbt lists
                if (list.g(i).getTypeId() != 10) {
                    continue;
                }

                NBTTagCompound nbt = (NBTTagCompound) list.g(i);
                String name = nbt.getString("Name");
                String attributeName = nbt.getString("AttributeName");

                // There is no offhand, or slot argument in 1_8_8.
                if (!"MechanicsCoreAttribute".equals(name) || !attribute.getMinecraftName().equals(attributeName)) {
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
                nbt.setLong("UUIDLeast", attribute.getUUID().getLeastSignificantBits());
                nbt.setLong("UUIDMost", attribute.getUUID().getMostSignificantBits());

                // Slot was added in 1_9_R1. Setting it in 1_8_R3 is probably
                // always redundant since servers on 1.8 are likely going to
                // stay on 1.8. The idea is if items are converted from 1.8 to
                // a higher version, the slot will then automatically work
                // properly.
                if (slot != null) {
                    nbt.setString("Slot", slot.getSlotName());
                }
            }
        }

        bukkitItem.setItemMeta(CraftItemStack.getItemMeta(nmsItem));
    }

    @Nonnull
    @Override
    public net.minecraft.server.v1_8_R3.ItemStack getNMSStack(@Nonnull ItemStack bukkitStack) {
        return CraftItemStack.asNMSCopy(bukkitStack);
    }

    @Nonnull
    @Override
    public String getNBTDebug(@Nonnull ItemStack bukkitStack) {
        NBTTagCompound nbt = getNMSStack(bukkitStack).getTag();
        return nbt == null ? "null" : nbt.toString();
    }

    private NBTTagCompound getBukkitCompound(net.minecraft.server.v1_8_R3.ItemStack nmsStack, String plugin) {
        if (nmsStack.getTag() == null) {
            nmsStack.setTag(new NBTTagCompound());
        }

        NBTTagCompound nbt = nmsStack.getTag().getCompound("PublicBukkitValues");

        // If the nbt compound was just created, make sure it is added to the
        // internal map.
        if (nbt.isEmpty()) {
            nmsStack.getTag().set("PublicBukkitValues", nbt);
        }

        if (plugin == null) {
            return nbt;
        } else {
            NBTTagCompound pluginCompound = nbt.getCompound(plugin);
            if (pluginCompound.isEmpty()) {
                nbt.set(plugin, pluginCompound);
            }

            return pluginCompound;
        }
    }

    private String getTagName(String key) {
        return "MechanicsCore:" + key;
    }
}