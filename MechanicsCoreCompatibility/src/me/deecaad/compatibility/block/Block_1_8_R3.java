package me.deecaad.compatibility.block;

import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityFallingBlock;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block_1_8_R3 implements BlockCompatibility {

    private static final Field multiBlockChangeB;

    static {
        Class<?> multiBlockChangeClass = ReflectionUtil.getNMSClass("PacketPlayOutMultiBlockChange");
        multiBlockChangeB = ReflectionUtil.getField(multiBlockChangeClass, "b");
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
        IBlockData blockData = net.minecraft.server.v1_8_R3.Block.getByCombinedId(mat.getId() | data << 12);
        return new EntityFallingBlock(world, loc.getX(), loc.getY(), loc.getZ(), blockData);
    }

    @Override
    public Object createFallingBlock(Location loc, BlockState state) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("World cannot be null");
        }

        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(state.getX(), state.getY(), state.getZ());
        IBlockData blockData = world.c(pos).getBlockData();
        return new EntityFallingBlock(world, loc.getX(), loc.getY(), loc.getZ(), blockData);
    }

    @Override
    public Object getBlockMaskPacket(Block bukkitBlock, org.bukkit.Material mask, byte data) {
        IBlockData blockData = net.minecraft.server.v1_8_R3.Block.getByCombinedId(mask.getId() | data << 12);

        return getBlockMaskPacket(bukkitBlock, blockData);
    }

    @Override
    public Object getBlockMaskPacket(Block bukkitBlock, BlockState mask) {
        WorldServer world = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        IBlockData blockData = world.c(pos).getBlockData();

        return getBlockMaskPacket(bukkitBlock, blockData);
    }

    private PacketPlayOutBlockChange getBlockMaskPacket(Block bukkitBlock, IBlockData mask) {

        CraftBlock block = ((CraftBlock) bukkitBlock);
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        World world = ((CraftWorld) block.getWorld()).getHandle();

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(world, position);
        packet.block = mask;

        return packet;
    }

    @Override
    public List<Object> getMultiBlockMaskPacket(List<Block> blocks, @Nullable org.bukkit.Material mask, byte data) {
        if (blocks == null || blocks.size() <= 0) {
            throw new IllegalArgumentException("No blocks are being changed!");
        }

        Map<Chunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(block.getChunk(), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());
        IBlockData theMask = mask == null ? null : net.minecraft.server.v1_8_R3.Block.getByCombinedId(mask.getId() | data << 12);

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

        Map<org.bukkit.Chunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(block.getChunk(), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());

        WorldServer world = ((CraftWorld) mask.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(mask.getX(), mask.getY(), mask.getZ());
        IBlockData blockData = world.c(pos).getBlockData();

        for (List<Block> entry : sortedBlocks.values()) {
            packets.add(getMultiBlockMaskPacket(entry, blockData));
        }

        return packets;
    }

    private PacketPlayOutMultiBlockChange getMultiBlockMaskPacket(List<Block> blocks, @Nullable IBlockData mask) {

        // It is assumed that all blocks are in the same chunk.
        // If blocks are not in the same chunk, the locations
        // that the mask occurs will be a bit "odd", but there
        // shouldn't be any issues (Other then masks occuring in
        // the wrong chunk)
        net.minecraft.server.v1_8_R3.Chunk chunk = ((CraftChunk) blocks.get(0).getChunk()).getHandle();

        // Setup default information
        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(0, new short[0], chunk);
        PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] changes
                = new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[blocks.size()];

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);

            // Where the block is relative to the chunk it is in
            int x = block.getX() & 0xF;
            int y = block.getY();
            int z = block.getZ() & 0xF;

            // Setting the (x, y, z) location into VarInt format
            short location = (short) (x << 12 | y | z << 8);

            // If mask is null, then undo the mask. Otherwise set the mask
            if (mask == null) {
                changes[i] = packet.new MultiBlockChangeInfo(location, chunk);
            } else {
                changes[i] = packet.new MultiBlockChangeInfo(location, mask);
            }
        }

        ReflectionUtil.setField(multiBlockChangeB, packet, changes);
        return packet;
    }
}
