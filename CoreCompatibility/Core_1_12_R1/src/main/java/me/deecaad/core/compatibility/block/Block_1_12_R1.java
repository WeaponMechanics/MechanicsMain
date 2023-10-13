package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class Block_1_12_R1 implements BlockCompatibility {

    private static final Field multiBlockChangeB;
    private static final Field durabilityField;
    private static final Field[] soundFields;

    static {
        Class<?> multiBlockChangeClass = ReflectionUtil.getPacketClass("PacketPlayOutMultiBlockChange");
        multiBlockChangeB = ReflectionUtil.getField(multiBlockChangeClass, "b");

        Class<?> blockClass = ReflectionUtil.getNMSClass("", "Block");
        durabilityField = ReflectionUtil.getField(blockClass, "durability");

        if (ReflectionUtil.getMCVersion() != 12) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Block_1_12_R1.class + " when not using Minecraft 12",
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
        if (!allowLiquid && isLiquid) return null;

        if (isLiquid) {
            HitBox hitBox = new HitBox(block.getX(), block.getY(), block.getZ(), block.getX() + 1, block.getY() + 1, block.getZ() + 1);
            hitBox.setBlockHitBox(block);
            return hitBox;
        }

        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = worldServer.getType(blockPosition);
        net.minecraft.server.v1_12_R1.Block nmsBlock = blockData.getBlock();

        // Passable block check -> false means passable (thats why !)
        if (!(blockData.d(worldServer, blockPosition) != net.minecraft.server.v1_12_R1.Block.k && nmsBlock.a(blockData, false))) return null;

        AxisAlignedBB aabb = blockData.e(worldServer, blockPosition);
        // 1.12 -> e
        // 1.11 -> d
        // 1.9 - 1.10 -> c

        int x = blockPosition.getX(), y = blockPosition.getY(), z = blockPosition.getZ();
        HitBox hitBox = new HitBox(x + aabb.a, y + aabb.b, z + aabb.c, x + aabb.d, y + aabb.e, z + aabb.f);
        hitBox.setBlockHitBox(block);
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

    @SuppressWarnings("deprecation")
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
        IBlockData theMask = mask == null ? null : net.minecraft.server.v1_12_R1.Block.getByCombinedId(mask.getId() | data << 12);

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

        net.minecraft.server.v1_12_R1.Chunk chunk = ((CraftChunk) blocks.get(0).getChunk()).getHandle();

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
        net.minecraft.server.v1_12_R1.Block nmsBlock = world.c(pos).getBlock();

        return (float) ReflectionUtil.invokeField(durabilityField, nmsBlock) / 5.0f;
    }

    @Override
    public SoundData getBlockSound(Object blockData, SoundType type) {
        MaterialData mat = (MaterialData) blockData;
        IBlockData block = net.minecraft.server.v1_12_R1.Block.getByCombinedId(mat.getItemTypeId() & mat.getData() << 12);
        SoundEffectType sounds = block.getBlock().getStepSound();

        SoundData soundData = new SoundData();
        soundData.type = type;
        soundData.pitch = sounds.n;
        soundData.volume = sounds.m;

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
        MinecraftKey key = SoundEffect.a.b(sound);
        return Sound.valueOf(key.getKey().replaceAll("\\.", "_").toUpperCase(Locale.ROOT));
    }
}