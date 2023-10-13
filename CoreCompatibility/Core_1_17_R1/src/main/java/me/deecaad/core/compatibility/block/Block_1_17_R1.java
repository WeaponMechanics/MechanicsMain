package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://nms.screamingsandals.org/1.17.1/
public class Block_1_17_R1 implements BlockCompatibility {

    private static final Class<?> multiBlockPacket;
    private static final Constructor<?> multiBlockPacketConstructor;
    private static final Constructor<?> shortSetConstructor;
    private static final Field multiBlockChangeB;
    private static final Field multiBlockChangeC;


    static {
        Class<?> shortClass = CompatibilityAPI.isPaper()
                ? ReflectionUtil.getClass("it.unimi.dsi.fastutil.shorts.ShortSet")
                : ReflectionUtil.getClass("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortSet");
        Class<?> shortArrayClass = CompatibilityAPI.isPaper()
                ? ReflectionUtil.getClass("it.unimi.dsi.fastutil.shorts.ShortArraySet")
                : ReflectionUtil.getClass("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortArraySet");
        shortSetConstructor = ReflectionUtil.getConstructor(shortArrayClass);

        multiBlockPacket = ReflectionUtil.getPacketClass("PacketPlayOutMultiBlockChange");
        multiBlockPacketConstructor = ReflectionUtil.getConstructor(multiBlockPacket, SectionPos.class, shortClass, LevelChunkSection.class, boolean.class);
        multiBlockChangeB = ReflectionUtil.getField(multiBlockPacket, "b");
        multiBlockChangeC = ReflectionUtil.getField(multiBlockPacket, "c");



        if (ReflectionUtil.getMCVersion() != 17) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Block_1_17_R1.class + " when not using Minecraft 17",
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
    public @NotNull Object getCrackPacket(@NotNull Block block, int crack, int id) {
        BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
        return new ClientboundBlockDestructionPacket(id, pos, crack);
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
        BlockState theMask = mask == null ? null : ((CraftBlockData) mask.createBlockData()).getState();

        for (List<Block> entry : sortedBlocks.values()) {
            packets.add(getMultiBlockMaskPacket(entry, theMask));
        }

        return packets;
    }

    @Override
    public @NotNull List<Object> getMultiBlockMaskPacket(@NotNull List<Block> blocks, @Nullable org.bukkit.block.BlockState mask) {
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("No blocks are being changed!");
        }

        Map<SubChunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(SubChunk.byBlock(block), chunk -> new ArrayList<>());
            list.add(block);
        }

        List<Object> packets = new ArrayList<>(sortedBlocks.size());
        BlockState theMask = mask == null ? null : ((CraftBlockState) mask).getHandle();

        for (List<Block> entry : sortedBlocks.values()) {
            packets.add(getMultiBlockMaskPacket(entry, theMask));
        }

        return packets;
    }

    private ClientboundSectionBlocksUpdatePacket getMultiBlockMaskPacket(List<Block> blocks, @Nullable BlockState mask) {

        BlockPos position = ((CraftBlock) blocks.get(0)).getPosition();

        // Setup default information
        short[] locations = new short[blocks.size()];
        BlockState[] data = new BlockState[blocks.size()];

        for (int i = 0; i < locations.length; i++) {
            Block block = blocks.get(i);

            int x = block.getX() & 0xF;
            int y = block.getY() & 0xF;
            int z = block.getZ() & 0xF;

            short shortLocation = (short) (x << 8 | z << 4 | y);
            locations[i] = shortLocation;
            data[i] = mask;
        }

        ClientboundSectionBlocksUpdatePacket packet = (ClientboundSectionBlocksUpdatePacket) ReflectionUtil.newInstance(multiBlockPacketConstructor, SectionPos.of(position), ReflectionUtil.newInstance(shortSetConstructor), null, false);
        ReflectionUtil.setField(multiBlockChangeB, packet, locations);
        ReflectionUtil.setField(multiBlockChangeC, packet, data);

        return packet;
    }
}