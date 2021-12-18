package me.deecaad.core.compatibility.block;

import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutMultiBlockChange;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortArraySet;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block_1_17_R1  implements BlockCompatibility {

    private static final Field multiBlockChangeB;
    private static final Field multiBlockChangeC;

    static {
        Class<?> multiBlockChangeClass = ReflectionUtil.getPacketClass("PacketPlayOutMultiBlockChange");
        multiBlockChangeB = ReflectionUtil.getField(multiBlockChangeClass, "b");
        multiBlockChangeC = ReflectionUtil.getField(multiBlockChangeClass, "c");
    }

    @Override
    public @NotNull Object getCrackPacket(@NotNull Block block, int crack) {

        // TODO: change usage to player.sendBlockDamage(location, float) <- since 1_16_R3

        int id = IDS.incrementAndGet();
        if (id == Integer.MAX_VALUE) {
            IDS.set(0);
        }

        return getCrackPacket(block, crack, id);
    }

    @Override
    public @NotNull Object getCrackPacket(@Nonnull Block block, int crack, int id) {

        // TODO: change usage to player.sendBlockDamage(location, float) <- since 1_16_R3

        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        return new PacketPlayOutBlockBreakAnimation(id, pos, crack);
    }

    @Override
    public @NotNull Object getBlockMaskPacket(@NotNull Block bukkitBlock, org.bukkit.Material mask, byte data) {

        // TODO: change usage to player.sendBlockChange(location, blockdata) <- since 1_13_R2
        // player.sendBlockChange(location, material, byte) <- 1_8_R3 - 1_12_R1
        // Actually this wont even need to be used through compatibility, simple version check while calling those methods is enough

        return getBlockMaskPacket(bukkitBlock, ((CraftBlockData) mask.createBlockData()).getState());
    }

    @Override
    public @NotNull Object getBlockMaskPacket(@NotNull Block bukkitBlock, @NotNull BlockState mask) {

        // TODO: change usage to player.sendBlockChange(location, blockdata) <- since 1_13_R2
        // player.sendBlockChange(location, material, byte) <- 1_8_R3 - 1_12_R1
        // Actually this wont even need to be used through compatibility, simple version check while calling those methods is enough

        return getBlockMaskPacket(bukkitBlock, ((CraftBlockState) mask).getHandle());
    }

    private PacketPlayOutBlockChange getBlockMaskPacket(Block bukkitBlock, IBlockData mask) {

        // TODO: change usage to player.sendBlockChange(location, blockdata) <- since 1_13_R2
        // player.sendBlockChange(location, material, byte) <- 1_8_R3 - 1_12_R1
        // Actually this wont even need to be used through compatibility, simple version check while calling those methods is enough

        CraftBlock block = ((CraftBlock) bukkitBlock);
        BlockPosition position = block.getPosition();

        return new PacketPlayOutBlockChange(position, mask);
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