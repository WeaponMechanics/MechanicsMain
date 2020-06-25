package me.deecaad.compatibility.item.dropped;

import net.minecraft.server.v1_15_R1.EntityItem;
import net.minecraft.server.v1_15_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class Drop_1_15_R1 implements DropCompatibility {

    @Override
    public Object toNMSItemEntity(ItemStack item, Location location) {

        World world = ((CraftWorld) location.getWorld()).getHandle();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        return new EntityItem(world, x, y, z, nmsItem);
    }
}
