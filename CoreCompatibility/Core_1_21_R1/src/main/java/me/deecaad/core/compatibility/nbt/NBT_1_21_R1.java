package me.deecaad.core.compatibility.nbt;

import com.cjcrafter.foliascheduler.util.FieldAccessor;
import com.cjcrafter.foliascheduler.util.ReflectionUtil;
import com.google.common.collect.Lists;
import me.deecaad.core.utils.StringUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

// https://nms.screamingsandals.org/1.19_R1
public class NBT_1_21_R1 extends NBT_Persistent {

    @Override
    public @NotNull String getNBTDebug(@NotNull org.bukkit.inventory.ItemStack bukkitStack) {
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(bukkitStack);
        if (nmsStack.isEmpty()) {
            return "null";
        }

        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        net.minecraft.nbt.Tag rawTag;
        try {
            rawTag = nmsStack.save(nmsServer.registryAccess());
        } catch (IllegalStateException ex) {
            // Thrown by nmsStack.save(...) if the stack is truly empty
            return "null";
        }

        if (!(rawTag instanceof net.minecraft.nbt.CompoundTag compoundTag)) {
            return "null";
        }

        return new TagColorVisitor().visit(compoundTag);
    }

    private static class TagColorVisitor extends StringTagVisitor {

        private static final String BRACE_COLORS = "f780"; // grayscale colors
        private static final String VALUE_COLORS = "6abcdef"; // bright colors
        private final StringBuilder builder;

        // Stores how many nested compound tags there currently are. Used to
        // determine curly brace color, as well as spacing.
        private final int indents;
        private final int colorOffset;

        public TagColorVisitor() {
            this(0, 0);
        }

        public TagColorVisitor(int indents, int colorOffset) {
            FieldAccessor field = ReflectionUtil.getField(StringTagVisitor.class, StringBuilder.class);
            this.builder = (StringBuilder) field.get(this);
            this.indents = indents;
            this.colorOffset = colorOffset;
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

                builder.append(StringUtil.repeat("  ", indents));

                String key = list.get(i);
                Tag value = Objects.requireNonNull(compound.get(key), "This is impossible");
                String color = "&" + VALUE_COLORS.charAt((i + colorOffset) % VALUE_COLORS.length());

                builder.append(color).append(handleEscape(key))
                    .append("&f&l: ").append(color)
                    .append(new TagColorVisitor(value instanceof CompoundTag ? indents + 1 : indents, colorOffset + i).visit(value));
            }

            builder.append(braceColor).append("}\n");
        }
    }
}