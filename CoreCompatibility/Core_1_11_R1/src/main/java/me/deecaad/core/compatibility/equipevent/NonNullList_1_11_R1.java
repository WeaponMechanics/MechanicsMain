package me.deecaad.core.compatibility.equipevent;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_11_R1.Item;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.NonNullList;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class NonNullList_1_11_R1 extends NonNullList<ItemStack> {

    private static final Field itemField = ReflectionUtil.getField(ItemStack.class, Item.class);

    static {
        if (ReflectionUtil.getMCVersion() != 11) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + NonNullList_1_11_R1.class + " when not using Minecraft 11",
                    new InternalError()
            );
        }
    }

    private final TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer;

    public NonNullList_1_11_R1(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        super(generate(size), ItemStack.a);

        this.consumer = consumer;
    }

    @Override
    public ItemStack set(int index, ItemStack newItem) {
        ItemStack oldItem = get(index);

        if (newItem.getCount() == 0 && ReflectionUtil.invokeField(itemField, newItem) != null) {
            newItem.setCount(1);
            consumer.accept(CraftItemStack.asBukkitCopy(oldItem), CraftItemStack.asBukkitCopy(newItem), index);
            newItem.setCount(0);
        }

        else if (oldItem.getCount() == 0 && ReflectionUtil.invokeField(itemField, oldItem) != null) {
            oldItem.setCount(1);
            consumer.accept(CraftItemStack.asBukkitCopy(oldItem), CraftItemStack.asBukkitCopy(newItem), index);
            oldItem.setCount(0);
        }

        else if (!ItemStack.matches(oldItem, newItem)) {
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
