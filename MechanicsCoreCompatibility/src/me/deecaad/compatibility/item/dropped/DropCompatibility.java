package me.deecaad.compatibility.item.dropped;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface DropCompatibility {

    Object toNMSItemEntity(ItemStack item, Location location);

}
