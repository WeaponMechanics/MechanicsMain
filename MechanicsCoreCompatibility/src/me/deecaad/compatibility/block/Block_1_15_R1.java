package me.deecaad.compatibility.block;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockBreakAnimation;
import org.bukkit.block.Block;

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
}
