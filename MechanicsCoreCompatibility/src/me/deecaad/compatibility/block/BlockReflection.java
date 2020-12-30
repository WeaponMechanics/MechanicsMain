package me.deecaad.compatibility.block;

import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortArraySet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.core.utils.ReflectionUtil.*;

public class BlockReflection implements BlockCompatibility {

    private static final Field packetMaskField;
    private static final Field positionsField;
    private static final Field masksField;

    private static final Method getBlockState;
    private static final Method getBlockHandle;
    private static final Method getWorldHandle;
    private static final Method getChunk;
    private static final Method createSectionPosition;

    private static final Constructor<?> blockPosConstructor;
    private static final Constructor<?> blockCrackPacketConstructor;
    private static final Constructor<?> blockMaskPacketConstructor;
    private static final Constructor<?> multiBlockMaskPacketConstructor;

    static {
        Class<?> blockPosClass = getNMSClass("BlockPosition");

        packetMaskField = getField(getNMSClass("PacketPlayOutBlockChange"), "block");
        positionsField = getField(getNMSClass("PacketPlayOutMultiBlockChange"), null, short[].class);
        masksField = getField(getNMSClass("PacketPlayOutMultiBlockChange"), null, Array.newInstance(getNMSClass("IBlockData"), 0).getClass());

        getBlockState = getMethod(getCBClass("block.data.CraftBlockData"), "getState");
        getBlockHandle = getMethod(getCBClass("block.CraftBlockState"), "getHandle");
        getWorldHandle = getMethod(getCBClass("CraftWorld"), "getHandle");
        getChunk = getMethod(getNMSClass("World"), "getChunkAt", int.class, int.class);
        createSectionPosition = getMethod(getNMSClass("SectionPosition"), getNMSClass("SectionPosition"), int.class, int.class, int.class);

        blockPosConstructor = getConstructor(blockPosClass, int.class, int.class, int.class);
        blockCrackPacketConstructor = getConstructor(getNMSClass("PacketPlayOutBlockBreakAnimation"), int.class, blockPosClass, int.class);
        blockMaskPacketConstructor = getConstructor(getNMSClass("PacketPlayOutBlockChange"), getNMSClass("Chunk"), blockPosClass);
        multiBlockMaskPacketConstructor = getConstructor(getNMSClass("PacketPlayOutMultiBlockChange"), getNMSClass("SectionPosition"), ShortArraySet.class, getNMSClass("ChunkSection"), boolean.class);
    }

    @Override
    public Object getCrackPacket(Block block, int crack) {

        int id = IDS.incrementAndGet();
        if (id == Integer.MAX_VALUE) {
            IDS.set(0);
        }

        return getCrackPacket(block, crack, id);
    }

    @Override
    public Object getCrackPacket(@Nonnull Block block, int crack, int id) {
        Object blockPos = ReflectionUtil.newInstance(blockPosConstructor, block.getX(), block.getY(), block.getZ());
        return ReflectionUtil.newInstance(blockCrackPacketConstructor, id, blockPos, crack);
    }

    @Override
    public Object getBlockMaskPacket(Block bukkitBlock, org.bukkit.Material mask, byte data) {
        return getBlockMaskPacket(bukkitBlock, invokeMethod(getBlockState, mask.createBlockData()));
    }

    @Override
    public Object getBlockMaskPacket(Block bukkitBlock, BlockState mask) {
        return getBlockMaskPacket(bukkitBlock, invokeMethod(getBlockHandle, mask));
    }

    private Object getBlockMaskPacket(Block block, Object mask) {

        int x = block.getChunk().getX();
        int z = block.getChunk().getZ();

        Object blockPos = newInstance(blockPosConstructor, block.getX(), block.getY(), block.getZ());
        Object world = invokeMethod(getWorldHandle, block.getWorld());
        Object chunk = invokeMethod(getChunk, world, x, z);

        Object packet = newInstance(blockMaskPacketConstructor, chunk, blockPos);
        setField(packetMaskField, packet, mask);

        return packet;
    }

    @Override
    public List<Object> getMultiBlockMaskPacket(List<Block> blocks, @Nullable org.bukkit.Material mask, byte data) {
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("No blocks are being changed!");
        }

        Map<SubChunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(SubChunk.byBlock(block), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());
        Object theMask = mask == null ? null : invokeMethod(getBlockState, mask.createBlockData());

        for (List<Block> entry : sortedBlocks.values()) {
            packets.add(getMultiBlockMaskPacket(entry, theMask));
        }

        return packets;
    }

    @Override
    public List<Object> getMultiBlockMaskPacket(List<Block> blocks, @Nullable BlockState mask) {
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("No blocks are being changed!");
        }

        Map<SubChunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(SubChunk.byBlock(block), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());
        Object theMask = invokeMethod(getBlockHandle, mask);

        for (List<Block> entry : sortedBlocks.values()) {
            packets.add(getMultiBlockMaskPacket(entry, theMask));
        }

        return packets;
    }

    private Object getMultiBlockMaskPacket(List<Block> blocks, @Nullable Object mask) {

        // Setup default information
        short[] locations = new short[blocks.size()];
        Object[] data = new Object[blocks.size()];

        for (int i = 0; i < locations.length; i++) {
            Block block = blocks.get(i);

            int x = block.getX() & 0xF;
            int y = block.getY() & 0xF;
            int z = block.getZ() & 0xF;

            short shortLocation = (short) (x << 8 | z << 4 | y);
            locations[i] = shortLocation;
            data[i] = mask;
        }

        Block block = blocks.get(0);
        int x = block.getX() >> 4;
        int y = block.getY() >> 4;
        int z = block.getZ() >> 4;
        Object sectionPosition = invokeMethod(createSectionPosition, null, x, y, z);

        Object packet = newInstance(multiBlockMaskPacketConstructor, sectionPosition, new ShortArraySet(0), null, false);
        ReflectionUtil.setField(positionsField, packet, locations);
        ReflectionUtil.setField(masksField, packet, data);

        return packet;
    }
}
