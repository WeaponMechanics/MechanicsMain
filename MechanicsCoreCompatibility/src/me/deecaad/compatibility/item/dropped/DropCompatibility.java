package me.deecaad.compatibility.item.dropped;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public interface DropCompatibility {

    Object toNMSItemEntity(ItemStack item, World world, double x, double y, double z);

}
