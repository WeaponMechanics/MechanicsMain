package me.deecaad.core.compatibility.nbt;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

// https://nms.screamingsandals.org/1.18.1/
public class NBT_1_18_R1 implements NBTCompatibility {

    @Nonnull
    @Override
    public net.minecraft.world.item.ItemStack getNMSStack(@Nonnull ItemStack bukkitStack) {
        return CraftItemStack.asNMSCopy(bukkitStack);
    }

    @Nonnull
    @Override
    public ItemStack getBukkitStack(@Nonnull Object nmsStack) {
        return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack) nmsStack);
    }

    @Nonnull
    @Override
    public String getNBTDebug(@Nonnull ItemStack bukkitStack) {
        CompoundTag nbt = getNMSStack(bukkitStack).getTag();
        return nbt == null ? "null" : nbt.toString();
    }
}