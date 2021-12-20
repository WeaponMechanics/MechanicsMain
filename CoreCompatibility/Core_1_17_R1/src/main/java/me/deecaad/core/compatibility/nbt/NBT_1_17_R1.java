package me.deecaad.core.compatibility.nbt;

import me.deecaad.core.compatibility.v1_17_R1;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class NBT_1_17_R1 implements NBTCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 17) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + NBT_1_17_R1.class + " when not using Minecraft 17",
                    new InternalError()
            );
        }
    }

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
        NBTTagCompound nbt = getNMSStack(bukkitStack).getTag();
        return nbt == null ? "null" : nbt.toString();
    }
}