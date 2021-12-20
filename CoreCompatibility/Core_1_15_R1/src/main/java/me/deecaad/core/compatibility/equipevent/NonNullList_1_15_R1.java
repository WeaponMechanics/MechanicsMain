package me.deecaad.core.compatibility.equipevent;

import me.deecaad.core.compatibility.v1_15_R1;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.NonNullList;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;

import java.util.Arrays;
import java.util.List;

public class NonNullList_1_15_R1 extends NonNullList<ItemStack> {

    static {
        if (ReflectionUtil.getMCVersion() != 15) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + NonNullList_1_15_R1.class + " when not using Minecraft 15",
                    new InternalError()
            );
        }
    }

    private final TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer;

    public NonNullList_1_15_R1(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        super(generate(size), ItemStack.a);

        this.consumer = consumer;
    }

    @Override
    public ItemStack set(int index, ItemStack newItem) {
        ItemStack oldItem = get(index);

        // Extra check
        if (!ItemStack.matches(oldItem, newItem)) {
            consumer.accept(CraftItemStack.asBukkitCopy(oldItem), CraftItemStack.asBukkitCopy(newItem), index);
        }

        return super.set(index, newItem);
    }

    private static List<ItemStack> generate(int size) {
        ItemStack[] items = new ItemStack[size];
        Arrays.fill(items, ItemStack.a);
        return Arrays.asList(items);
    }
}
