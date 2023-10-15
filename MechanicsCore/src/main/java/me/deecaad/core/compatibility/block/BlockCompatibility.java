package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.compatibility.ICompatibility;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This interface outlines a version dependant api that return values based on
 * different {@link Block} inputs. There should be an implementing class for
 * each minecraft protocol version.
 *
 * <p>For methods that return packets, in order for those packets to be visible
 * to players, the packets need to be sent to the players. See
 * {@link ICompatibility#sendPackets(Player, Object)}.
 */
public interface BlockCompatibility {

    /**
     * Threadsafe method to get unique ids for block cracking.
     *
     * @see #getCrackPacket(Block, int)
     */
    AtomicInteger IDS = new AtomicInteger(0);

    /**
     * If block is air, or some other passable block (e.g. torch, flower)
     * then this method WILL always return null. Basically if this method returns null
     * means that block is passable.
     *
     * @param block the block
     * @return the block's hit box or null if it's passable for example
     */
    default HitBox getHitBox(Block block) {
        return getHitBox(block, false);
    }

    /**
     * If block is air, or some other passable block (e.g. torch, flower)
     * then this method WILL always return null. Basically if this method returns null
     * means that block is passable.
     *
     * @param block the block
     * @param allowLiquid whether liquid should be considered as having hit box
     * @return the block's hit box or null if it's passable for example
     */
    default HitBox getHitBox(Block block, boolean allowLiquid) {
        // This default should only be used after 1.17
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

        // This default should only be used after 1.17 R1
        Collection<BoundingBox> voxelShape = block.getCollisionShape().getBoundingBoxes();
        if (voxelShape.size() > 1) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            for (BoundingBox boxPart : voxelShape) {
                hitBox.addVoxelShapePart(new HitBox(x + boxPart.getMinX(), y + boxPart.getMinY(), z + boxPart.getMinZ(),
                        x + boxPart.getMaxX(), y + boxPart.getMaxY(), z + boxPart.getMaxZ()));
            }
        }

        return hitBox;
    }

    /**
     * Returns a block break animation packet for the given <code>block</code>
     * and <code>crack</code>. This should probably not be used for transparent
     * blocks. This method is a shorthand for
     * {@link #getCrackPacket(Block, int, int)}.
     *
     * <p>For more information, please see the protocol
     * <a href="https://wiki.vg/Protocol#Block_Break_Animation">wiki</a>.
     *
     * @param block The non-null block to display the cracking animation over.
     * @param crack The cracking amount, between 0 and 9 inclusively. Higher
     *              values are more visibly cracked.
     * @return The non-null animation packet.
     */
    @NotNull
    Object getCrackPacket(@NotNull Block block, int crack);

    /**
     * Returns a block break animation packet for the given <code>block</code>
     * and <code>crack</code>. This should probably not be used for transparent
     * blocks. The <code>id</code> is a unique to each packet, and sending a
     * new packet with the same id will cause the previous one to be
     * overwritten.
     *
     * <p>For more information, please see the protocol
     * <a href="https://wiki.vg/Protocol#Block_Break_Animation">wiki</a>.
     *
     * @param block The non-null block to display the cracking animation over.
     * @param crack The cracking amount, between 0 and 9 inclusively. Higher
     *              values are more visibly cracked.
     * @param id    The unique id. If you do not want to override the previous
     *              packet, use {@link #getCrackPacket(Block, int)}}.
     * @return The non-null animation packet.
     */
    @NotNull
    Object getCrackPacket(@NotNull Block block, int crack, int id);

    /**
     * Returns a list of multi block change packets that masks all of the given
     * <code>blocks</code>. This packet will make the block <i>appear</i> as
     * the given <code>mask</code>. The <code>data</code> is used in legacy
     * minecraft versions, and is ignored in newer versions. The mask for each
     * individual block is removed if it is interacted with.
     *
     * For each {@link org.bukkit.Chunk} that is included in
     * <code>blocks</code>, there is another packet added. Note that in
     * version v1_16_R2 and higher, a new packet
     * is used for each {@link SubChunk}.
     *
     * <p>For more information, please see the protocol
     * <a href="https://wiki.vg/Protocol#Multi_Block_Change">wiki</a>.
     *
     * @param blocks The non-null list of non-null blocks to mask.
     * @param mask   The non-null bukkit material for the mask.
     * @param data   The non-negative byte data for material for legacy
     *               minecraft versions. For newer versions, this data should
     *               be ignored.
     * @return The non-null list of non-null block mask packets.
     */
    @NotNull
    List<Object> getMultiBlockMaskPacket(@NotNull List<Block> blocks, @NotNull Material mask, byte data);

    /**
     * Returns a list of multi block change packets that masks all given
     * <code>blocks</code>. The packet will make the block <i>appear</i> as the
     * given <code>mask</code>. The mask for each individual block is removed
     * if it is interacted with.
     *
     * For each {@link org.bukkit.Chunk} that is included in
     * <code>blocks</code>, there is another packet added. Note that in
     * version v1_16_R2 and higher, a new packet
     * is used for each {@link SubChunk}.
     *
     * <p>For more information, please see the protocol
     * <a href="https://wiki.vg/Protocol#Multi_Block_Change">wiki</a>.
     *
     * @param blocks The non-null list of non-null blocks to mask.
     * @param mask   The non-null state to mask the block as.
     * @return The non-null list of non-null block mask packets.
     */
    @NotNull
    List<Object> getMultiBlockMaskPacket(@NotNull List<Block> blocks, @Nullable BlockState mask);

    default SoundData getBlockSound(Object blockData, SoundType type) {
        BlockData data = (BlockData) blockData;
        SoundGroup sounds = data.getSoundGroup();

        SoundData soundData = new SoundData();
        soundData.type = type;
        soundData.pitch = sounds.getPitch();
        soundData.volume = sounds.getVolume();

        soundData.sound = switch (type) {
            case BREAK -> sounds.getBreakSound();
            case STEP -> sounds.getStepSound();
            case PLACE -> sounds.getPlaceSound();
            case HIT -> sounds.getHitSound();
            case FALL ->  sounds.getFallSound();
        };

        return soundData;
    }

    /**
     * Returns a positive float representing the blast material of a given
     * block. Materials with a higher blast resistance are less likely to
     * be destroyed by explosions.
     *
     * @param block The non-null bukkit block.
     * @return Positive float representing the blast resistance.
     */
    default float getBlastResistance(Block block) {
        return block.getType().getBlastResistance();
    }

    class SoundData {
        public SoundType type;
        public Sound sound;
        public float volume;
        public float pitch;
    }

    enum SoundType {
        BREAK, STEP, PLACE, HIT, FALL
    }
}