package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class Block_1_14_R1 implements BlockCompatibility {

    private static final Field multiBlockChangeB;
    private static final Field[] soundFields;

    static {
        Class<?> multiBlockChangeClass = ReflectionUtil.getPacketClass("PacketPlayOutMultiBlockChange");
        multiBlockChangeB = ReflectionUtil.getField(multiBlockChangeClass, "b");

        if (ReflectionUtil.getMCVersion() != 14) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Block_1_14_R1.class + " when not using Minecraft 14",
                    new InternalError()
            );
        }

        soundFields = new Field[SoundType.values().length]; // 5
        for (int i = 0; i < soundFields.length; i++) {
            soundFields[i] = ReflectionUtil.getField(SoundEffectType.class, SoundEffect.class, i);
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
    public @NotNull List<Object> getMultiBlockMaskPacket(@NotNull List<Block> blocks, @Nullable Material mask, byte data) {
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("No blocks are being changed!");
        }

        Map<Chunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(block.getChunk(), chunk -> new ArrayList<>());
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

        Map<Chunk, List<Block>> sortedBlocks = new HashMap<>();
        for (Block block : blocks) {
            List<Block> list = sortedBlocks.computeIfAbsent(block.getChunk(), chunk -> new ArrayList<>());
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

        net.minecraft.server.v1_14_R1.Chunk chunk = ((CraftChunk) blocks.get(0).getChunk()).getHandle();

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
    public SoundData getBlockSound(Object blockData, SoundType type) {
        CraftBlockData block = (CraftBlockData) blockData;
        SoundEffectType sounds = block.getState().r();

        SoundData soundData = new SoundData();
        soundData.type = type;
        soundData.pitch = sounds.x;
        soundData.volume = sounds.w;

        switch (type) {
            case BREAK -> soundData.sound = bukkit(sounds, 0);
            case STEP -> soundData.sound = bukkit(sounds, 1);
            case PLACE -> soundData.sound = bukkit(sounds, 2);
            case HIT -> soundData.sound = bukkit(sounds, 3);
            case FALL -> soundData.sound = bukkit(sounds, 4);
            default -> throw new InternalError("unreachable code");
        }

        return soundData;
    }

    private Sound bukkit(SoundEffectType sounds, int index) {
        SoundEffect sound = (SoundEffect) ReflectionUtil.invokeField(soundFields[index], sounds);
        MinecraftKey key = IRegistry.SOUND_EVENT.getKey(sound);
        return Sound.valueOf(key.getKey().replaceAll("\\.", "_").toUpperCase(Locale.ROOT));
    }
}