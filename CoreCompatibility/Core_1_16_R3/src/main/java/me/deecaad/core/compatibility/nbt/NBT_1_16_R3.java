package me.deecaad.core.compatibility.nbt;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class NBT_1_16_R3 implements NBTCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 16) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + NBT_1_16_R3.class + " when not using Minecraft 16",
                    new InternalError()
            );
        }
    }

    @Nonnull
    @Override
    public net.minecraft.server.v1_16_R3.ItemStack getNMSStack(@Nonnull ItemStack bukkitStack) {
        return CraftItemStack.asNMSCopy(bukkitStack);
    }

    @Nonnull
    @Override
    public ItemStack getBukkitStack(@Nonnull Object nmsStack) {
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_16_R3.ItemStack) nmsStack);
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
}