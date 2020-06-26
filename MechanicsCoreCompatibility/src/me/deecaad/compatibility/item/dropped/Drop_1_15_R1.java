package me.deecaad.compatibility.item.dropped;

import net.minecraft.server.v1_15_R1.EntityItem;
import net.minecraft.server.v1_15_R1.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class Drop_1_15_R1 implements DropCompatibility {

    @Override
    public Object toNMSItemEntity(ItemStack item, org.bukkit.World world, double x, double y, double z) {

        World nmsWorld = ((CraftWorld) world).getHandle();
        net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        return new EntityItem(nmsWorld, x, y, z, nmsItem);
    }
}
