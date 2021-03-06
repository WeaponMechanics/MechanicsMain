package me.deecaad.core.compatibility.block;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.IBlockData;
import net.minecraft.server.v1_10_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_10_R1.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_10_R1.WorldServer;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block_1_10_R1 implements BlockCompatibility {

    private static final Field multiBlockChangeB;
    private static final Field durabilityField;

    static {
        Class<?> multiBlockChangeClass = ReflectionUtil.getPacketClass("PacketPlayOutMultiBlockChange");
        multiBlockChangeB = ReflectionUtil.getField(multiBlockChangeClass, "b");

        Class<?> blockClass = ReflectionUtil.getNMSClass("", "Block");
        durabilityField = ReflectionUtil.getField(blockClass, "durability");

        if (ReflectionUtil.getMCVersion() != 10) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Block_1_10_R1.class + " when not using Minecraft 10",
                    new InternalError()
            );
        }
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
    public @NotNull Object getCrackPacket(@Nonnull Block block, int crack, int id) {
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        return new PacketPlayOutBlockBreakAnimation(id, pos, crack);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull List<Object> getMultiBlockMaskPacket(@NotNull List<Block> blocks, @Nullable org.bukkit.Material mask, byte data) {
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("No blocks are being changed!");
        }

        Map<Chunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(block.getChunk(), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());
        IBlockData theMask = mask == null ? null : net.minecraft.server.v1_10_R1.Block.getByCombinedId(mask.getId() | data << 12);

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

        Map<org.bukkit.Chunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(block.getChunk(), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());

        IBlockData blockData = null;
        if (mask != null) {
            WorldServer world = ((CraftWorld) mask.getWorld()).getHandle();
            BlockPosition pos = new BlockPosition(mask.getX(), mask.getY(), mask.getZ());
            blockData = world.c(pos).getBlock().getBlockData();
        }

        for (List<Block> entry : sortedBlocks.values()) {
            packets.add(getMultiBlockMaskPacket(entry, blockData));
        }

        return packets;
    }

    private PacketPlayOutMultiBlockChange getMultiBlockMaskPacket(List<Block> blocks, @Nullable IBlockData mask) {

        net.minecraft.server.v1_10_R1.Chunk chunk = ((CraftChunk) blocks.get(0).getChunk()).getHandle();

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

    @Override
    public float getBlastResistance(Block block) {
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        net.minecraft.server.v1_10_R1.Block nmsBlock = world.c(pos).getBlock();

        return (float) ReflectionUtil.invokeField(durabilityField, nmsBlock) / 5.0f;
    }
}
