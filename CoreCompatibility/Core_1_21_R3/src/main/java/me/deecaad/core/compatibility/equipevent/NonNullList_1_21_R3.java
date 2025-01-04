package me.deecaad.core.compatibility.equipevent;

import com.cjcrafter.foliascheduler.util.FieldAccessor;
import com.cjcrafter.foliascheduler.util.ReflectionUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

import java.util.Arrays;
import java.util.List;

public class NonNullList_1_21_R3 extends NonNullList<ItemStack> {

    private static final FieldAccessor itemField = ReflectionUtil.getField(ItemStack.class, Item.class);

    private final TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer;

    public NonNullList_1_21_R3(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        super(generate(size), ItemStack.EMPTY);

        this.consumer = consumer;
    }

    @Override
    public ItemStack set(int index, ItemStack newItem) {
        ItemStack oldItem = get(index);

        if (newItem.getCount() == 0 && itemField.get(newItem) != null) {
            newItem.setCount(1);
            consumer.accept(CraftItemStack.asBukkitCopy(oldItem), CraftItemStack.asBukkitCopy(newItem), index);
            newItem.setCount(0);
        }

        else if (oldItem.getCount() == 0 && itemField.get(oldItem) != null) {
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
        Arrays.fill(items, ItemStack.EMPTY);
        return Arrays.asList(items);
    }
}
