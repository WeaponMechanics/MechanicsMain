package me.deecaad.core.compatibility.nbt;

import com.google.common.collect.Lists;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// https://nms.screamingsandals.org/1.18.1/
public class NBT_1_18_R1 implements NBTCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 18) {
            MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + NBT_1_18_R1.class + " when not using Minecraft 18",
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
        CompoundTag nbt = getNMSStack(bukkitStack).getTag();
        return nbt == null ? "null" : new TagColorVisitor(0).visit(nbt);
    }

    private static class TagColorVisitor extends StringTagVisitor {

        private static final String BRACE_COLORS = "f780"; // grayscale colors
        private static final String VALUE_COLORS = "6abcdef"; // bright colors
        private final StringBuilder builder;

        // Stores how many nested compound tags there currently are. Used to
        // determine curly brace color, as well as spacing.
        private final int indents;

        public TagColorVisitor(int indents) {
            super(); // The parent's StringBuilder is set in the constructor

            Field field = ReflectionUtil.getField(super.getClass(), StringBuilder.class);
            this.builder = (StringBuilder) ReflectionUtil.invokeField(field, this);
            this.indents = indents;
        }

        @Override
        public void visitCompound(CompoundTag compound) {
            String braceColor = "&" + BRACE_COLORS.charAt(indents % BRACE_COLORS.length());
            builder.append(braceColor).append("{\n");
            List<String> list = Lists.newArrayList(compound.getAllKeys());
            Collections.sort(list);

            for (int i = 0; i < list.size(); i++) {

                // Add a new line after each element, and indent each line
                // depending on the number of nested CompoundTags.
                if (i != 0)
                    builder.append('\n');
                builder.append("  ".repeat(indents));

                String key = list.get(i);
                Tag value = Objects.requireNonNull(compound.get(key), "This is impossible");
                String color = "&" + VALUE_COLORS.charAt(indents % VALUE_COLORS.length());

                builder.append(color).append(handleEscape(key))
                        .append("&f&l: ").append(color)
                        .append(new TagColorVisitor(value instanceof CompoundTag ? indents + 1 : indents).visit(value));
            }

            builder.append("}\n");
        }
    }
}