package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.ICompatibility;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    @Nonnull
    Object getCrackPacket(@Nonnull Block block, int crack);

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
    @Nonnull
    Object getCrackPacket(@Nonnull Block block, int crack, int id);

    /**
     * Returns a block change packet for the given <code>bukkitBlock</code>.
     * This packet will make the block <i>appear</i> as the given
     * <code>mask</code> and <code>data</code>. The mask is removed if the
     * player interacts with the block.
     *
     * <p>For more information, please see the protocol
     * <a href="https://wiki.vg/Protocol#Block_Change">wiki</a>.
     *
     * @param bukkitBlock The non-null block to mask.
     * @param mask        The non-null bukkit material for the mask.
     * @param data        The non-negative byte data for material for legacy
     *                    minecraft versions. For newer versions, this data
     *                    should be ignored.
     * @return The non-null block mask packet.
     */
    @Nonnull
    Object getBlockMaskPacket(@Nonnull Block bukkitBlock, @Nonnull Material mask, @Nonnegative byte data);

    /**
     * Returns a block change packet for the given <code>bukkitBlock</code>.
     * This packet will make the block <i>appear</i> as the given
     * <code>mask</code>. The mask is removed if the player interacts with the
     * block.
     *
     * <p>For more information, please see the protocol
     * <a href="https://wiki.vg/Protocol#Block_Change">wiki</a>.
     *
     * @param bukkitBlock The non-null block to mask.
     * @param mask        The non-null state to mask the block as.
     * @return The non-null block mask packet.
     */
    @Nonnull
    Object getBlockMaskPacket(@Nonnull Block bukkitBlock, @Nonnull BlockState mask);

    /**
     * Returns a list of multi block change packets that masks all of the given
     * <code>blocks</code>. This packet will make the block <i>appear</i> as
     * the given <code>mask</code>. The <code>data</code> is used in legacy
     * minecraft versions, and is ignored in newer versions. The mask for each
     * individual block is removed if it is interacted with.
     *
     * For each {@link org.bukkit.Chunk} that is included in
     * <code>blocks</code>, there is another packet added. Note that in
     * version {@link net.minecraft.server.v1_16_R2} and higher, a new packet
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
    @Nonnull
    List<Object> getMultiBlockMaskPacket(@Nonnull List<Block> blocks, @Nonnull Material mask, @Nonnegative byte data);

    /**
     * Returns a list of multi block change packets that masks all given
     * <code>blocks</code>. The packet will make the block <i>appear</i> as the
     * given <code>mask</code>. The mask for each individual block is removed
     * if it is interacted with.
     *
     * For each {@link org.bukkit.Chunk} that is included in
     * <code>blocks</code>, there is another packet added. Note that in
     * version {@link net.minecraft.server.v1_16_R2} and higher, a new packet
     * is used for each {@link SubChunk}.
     *
     * <p>For more information, please see the protocol
     * <a href="https://wiki.vg/Protocol#Multi_Block_Change">wiki</a>.
     *
     * @param blocks The non-null list of non-null blocks to mask.
     * @param mask   The non-null state to mask the block as.
     * @return The non-null list of non-null block mask packets.
     */
    @Nonnull
    List<Object> getMultiBlockMaskPacket(@Nonnull List<Block> blocks, @Nullable BlockState mask);

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
}