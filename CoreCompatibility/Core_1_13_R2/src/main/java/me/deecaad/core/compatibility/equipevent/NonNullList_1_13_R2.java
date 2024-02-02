package me.deecaad.core.compatibility.equipevent;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_13_R2.Item;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.NonNullList;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class NonNullList_1_13_R2 extends NonNullList<ItemStack> {

    private static final Field itemField = ReflectionUtil.getField(ItemStack.class, Item.class);

    static {
        if (ReflectionUtil.getMCVersion() != 13) {
            me.deecaad.core.MechanicsCore.debug.log(
                LogLevel.ERROR,
                "Loaded " + NonNullList_1_13_R2.class + " when not using Minecraft 13",
                new InternalError());
        }
    }

    private final TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer;

    public NonNullList_1_13_R2(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
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
