package me.deecaad.compatibility.block;

import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityFallingBlock;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_16_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_16_R3.SectionPosition;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortArraySet;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block_1_16_R3 implements BlockCompatibility {

    private static final Field multiBlockChangeB;
    private static final Field multiBlockChangeC;

    static {
        Class<?> multiBlockChangeClass = ReflectionUtil.getNMSClass("PacketPlayOutMultiBlockChange");
        multiBlockChangeB = ReflectionUtil.getField(multiBlockChangeClass, "b");
        multiBlockChangeC = ReflectionUtil.getField(multiBlockChangeClass, "c");
    }

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
    public Object createFallingBlock(Location loc, org.bukkit.Material mat, byte data) {

        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
        IBlockData blockData = ((CraftBlockData) mat.createBlockData()).getState();
        return new EntityFallingBlock(world, loc.getX(), loc.getY(), loc.getZ(), blockData);
    }

    @Override
    public Object createFallingBlock(Location loc, BlockState state) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("World cannot be null");
        }

        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
        IBlockData blockData = ((CraftBlockState) state).getHandle();
        return new EntityFallingBlock(world, loc.getX(), loc.getY(), loc.getZ(), blockData);
    }

    @Override
    public Object getBlockMaskPacket(Block bukkitBlock, org.bukkit.Material mask, byte data) {
        return getBlockMaskPacket(bukkitBlock, ((CraftBlockData) mask.createBlockData()).getState());
    }

    @Override
    public Object getBlockMaskPacket(Block bukkitBlock, BlockState mask) {
        return getBlockMaskPacket(bukkitBlock, ((CraftBlockState) mask).getHandle());
    }

    private PacketPlayOutBlockChange getBlockMaskPacket(Block bukkitBlock, IBlockData mask) {

        CraftBlock block = ((CraftBlock) bukkitBlock);
        BlockPosition position = block.getPosition();
        World world = block.getCraftWorld().getHandle();

        int x = block.getChunk().getX();
        int z = block.getChunk().getZ();

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(world.getChunkAt(x, z), position);
        packet.block = mask;

        return packet;
    }

    @Override
    public List<Object> getMultiBlockMaskPacket(List<Block> blocks, @Nullable org.bukkit.Material mask, byte data) {
        if (blocks == null || blocks.size() <= 0) {
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
    public Object getMultiBlockMaskPacket(List<Block> blocks, @Nullable BlockState mask) {
        if (blocks == null || blocks.size() <= 0) {
            throw new IllegalArgumentException("No blocks are being changed!");
        }

        Map<SubChunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(SubChunk.byBlock(block), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());
        IBlockData theMask = ((CraftBlockState) mask).getHandle();

        for (List<Block> entry : sortedBlocks.values()) {
            packets.add(getMultiBlockMaskPacket(entry, theMask));
        }

        return packets;
    }

    private PacketPlayOutMultiBlockChange getMultiBlockMaskPacket(List<Block> blocks, @Nullable IBlockData mask) {

        // It is assumed that all blocks are in the same chunk.
        // If blocks are not in the same chunk, the locations
        // that the mask occurs will be a bit "odd", but there
        // shouldn't be any issues (Other then masks occuring in
        // the wrong chunk)
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

