package me.deecaad.compatibility.block;

import me.deecaad.core.utils.NumberUtils;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockBreakAnimation;
import org.bukkit.block.Block;

public class Block_1_15_R1 implements BlockCompatibility {

    @Override
    public Object getCrackPacket(Block block, int crack) {

        // While it's weird to generate a random int for
        // an id, it's apparently valid
        //
        // This will also cause the block to "stop" cracking
        // randomly (If the id is duplicated)
        int id = NumberUtils.random(1000);

        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        return new PacketPlayOutBlockBreakAnimation(id, pos, crack);
    }
}
