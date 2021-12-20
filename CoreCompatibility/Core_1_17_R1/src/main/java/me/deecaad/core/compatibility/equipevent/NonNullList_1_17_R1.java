package me.deecaad.core.compatibility.equipevent;

import me.deecaad.core.compatibility.v1_17_R1;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

import java.util.Arrays;
import java.util.List;

public class NonNullList_1_17_R1 extends NonNullList<ItemStack> {

    static {
        if (ReflectionUtil.getMCVersion() != 17) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + NonNullList_1_17_R1.class + " when not using Minecraft 17",
                    new InternalError()
            );
        }
    }

    private final TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer;

    public NonNullList_1_17_R1(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        super(generate(size), ItemStack.b);

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
        Arrays.fill(items, ItemStack.b);
        return Arrays.asList(items);
    }
}
