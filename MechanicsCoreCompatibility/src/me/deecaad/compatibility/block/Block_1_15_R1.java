package me.deecaad.compatibility.block;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.EntityFallingBlock;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.util.Vector;

public class Block_1_15_R1 implements BlockCompatibility {

    @Override
    public Object getCrackPacket(Block block, int crack) {

        int id = IDS.incrementAndGet();
        if (id == Integer.MAX_VALUE) {
            IDS.set(0);
        }

        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        return new PacketPlayOutBlockBreakAnimation(id, pos, crack);
    }

    @Override
    public Object createFallingBlock(Block block, Vector vector) {

        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        IBlockData blockData = ((CraftBlockData) block.getBlockData()).getState();
        double x = block.getX() + 0.5;
        double y = block.getY() + 0.5;
        double z = block.getZ() + 0.5;
        EntityFallingBlock falling = new EntityFallingBlock(world, x, y, z, blockData);

        falling.setMot(vector.getX(), vector.getY(), vector.getZ());

        return falling;
    }
}
