package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.IRegistry;
import net.minecraft.server.v1_14_R1.MinecraftKey;
import net.minecraft.server.v1_14_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_14_R1.SoundEffect;
import net.minecraft.server.v1_14_R1.SoundEffectType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

public class Block_1_14_R1 implements BlockCompatibility {

    private static final Field[] soundFields;

    static {
        soundFields = new Field[SoundType.values().length]; // 5
        for (int i = 0; i < soundFields.length; i++) {
            soundFields[i] = ReflectionUtil.getField(SoundEffectType.class, SoundEffect.class, i);
        }
    }

    @Override
    public HitBox getHitBox(@NotNull Block block, boolean allowLiquid) {
        if (!block.getChunk().isLoaded())
            return null;
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