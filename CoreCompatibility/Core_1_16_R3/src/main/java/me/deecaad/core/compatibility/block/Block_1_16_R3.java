package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.HitBox;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockBreakAnimation;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Block_1_16_R3 implements BlockCompatibility {

    @Override
    public HitBox getHitBox(Block block, boolean allowLiquid) {
        if (block.isEmpty())
            return null;

        boolean isLiquid = block.isLiquid();
        if (!allowLiquid) {
            if (block.isPassable() || block.isLiquid())
                return null;
        } else if (!isLiquid && block.isPassable()) {
            // Check like this because liquid is also passable...
            return null;
        }

        HitBox hitBox;
        if (isLiquid) {
            hitBox = new HitBox(block.getX(), block.getY(), block.getZ(), block.getX() + 1, block.getY() + 1, block.getZ() + 1);
        } else {
            BoundingBox boundingBox = block.getBoundingBox();
            hitBox = new HitBox(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        }
        hitBox.setBlockHitBox(block);

        CraftBlock craftBlock = (CraftBlock) block;
        List<AxisAlignedBB> voxelShape = craftBlock.getNMS().getCollisionShape(craftBlock.getCraftWorld().getHandle(), craftBlock.getPosition()).d();
        if (voxelShape.size() > 1) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            for (AxisAlignedBB boxPart : voxelShape) {
                hitBox.addVoxelShapePart(new HitBox(x + boxPart.minX, y + boxPart.minY, z + boxPart.minZ,
                    x + boxPart.maxX, y + boxPart.maxY, z + boxPart.maxZ));
            }
        }

        return hitBox;
    }

    @Override
    public @NotNull Object getCrackPacket(@NotNull Block block, int crack) {
        int id = IDS.incrementAndGet();
        if (id == Integer.MAX_VALUE) {
            IDS.set(0);
        }

        return getCrackPacket(block, crack, id);
    }

    @Override
    public @NotNull Object getCrackPacket(@NotNull Block block, int crack, int id) {
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        return new PacketPlayOutBlockBreakAnimation(id, pos, crack);
    }
}
