package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortArraySet;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block_1_16_R3 implements BlockCompatibility {

    private static final Field multiBlockChangeB;
    private static final Field multiBlockChangeC;

    static {
        Class<?> multiBlockChangeClass = ReflectionUtil.getPacketClass("PacketPlayOutMultiBlockChange");
        multiBlockChangeB = ReflectionUtil.getField(multiBlockChangeClass, "b");
        multiBlockChangeC = ReflectionUtil.getField(multiBlockChangeClass, "c");

        if (ReflectionUtil.getMCVersion() != 16) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Block_1_16_R3.class + " when not using Minecraft 16",
                    new InternalError()
            );
        }
    }

    @Override
    public HitBox getHitBox(Block block, boolean allowLiquid) {
        if (block.isEmpty()) return null;

        boolean isLiquid = block.isLiquid();
        if (!allowLiquid) {
            if (block.isPassable() || block.isLiquid()) return null;
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

    @Override
    public @NotNull List<Object> getMultiBlockMaskPacket(@NotNull List<Block> blocks, @Nullable org.bukkit.Material mask, byte data) {
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("No blocks are being changed!");
        }

        Map<SubChunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(SubChunk.byBlock(block), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());
        IBlockData theMask = mask == null ? null : ((CraftBlockData) mask.createBlockData()).getState();

        for (List<Block> entry : sortedBlocks.values()) {
            packets.add(getMultiBlockMaskPacket(entry, theMask));
        }

        return packets;
    }

    @Override
    public @NotNull List<Object> getMultiBlockMaskPacket(@NotNull List<Block> blocks, @Nullable BlockState mask) {
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("No blocks are being changed!");
        }

        Map<SubChunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(SubChunk.byBlock(block), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());
        IBlockData theMask = mask == null ? null : ((CraftBlockState) mask).getHandle();

        for (List<Block> entry : sortedBlocks.values()) {
            packets.add(getMultiBlockMaskPacket(entry, theMask));
        }

        return packets;
    }

    private PacketPlayOutMultiBlockChange getMultiBlockMaskPacket(List<Block> blocks, @Nullable IBlockData mask) {

        BlockPosition position = ((CraftBlock) blocks.get(0)).getPosition();

        // Setup default information
        short[] locations = new short[blocks.size()];
        IBlockData[] data = new IBlockData[blocks.size()];

        for (int i = 0; i < locations.length; i++) {
            Block block = blocks.get(i);

            int x = block.getX() & 0xF;
            int y = block.getY() & 0xF;
            int z = block.getZ() & 0xF;

            short shortLocation = (short) (x << 8 | z << 4 | y);
            locations[i] = shortLocation;
            data[i] = mask;
        }

        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(SectionPosition.a(position), new ShortArraySet(0), null, false);
        ReflectionUtil.setField(multiBlockChangeB, packet, locations);
        ReflectionUtil.setField(multiBlockChangeC, packet, data);

        return packet;
    }
}

