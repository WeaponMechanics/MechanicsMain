package me.deecaad.compatibility.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface BlockCompatibility {


    AtomicInteger IDS = new AtomicInteger(0);

    /**
     * Gets a <code>PacketPlayOutBlockBreakAnimation</code> packet
     * for the given block
     *
     * https://wiki.vg/Protocol#Block_Break_Animation
     *
     * Crack values [0-9] will show the crack animation, anything
     * else will remove the animation.
     *
     * If this is used on a transparent block, weird graphical effects
     * may occur
     *
     * Note that in order for players to see the changes, you must
     * send them the returned packet
     * @see me.deecaad.compatibility.ICompatibility#sendPackets(Player, Object...)
     *
     * @param block The block to crack
     * @param crack The amount to crack
     * @return The constructed packet
     */
    Object getCrackPacket(@Nonnull Block block, int crack);

    /**
     * Gets a <code>PacketPlayOutBlockBreakAnimation</code> packet
     * for the given block
     *
     * https://wiki.vg/Protocol#Block_Break_Animation
     *
     * Crack values [0-9] will show the crack animation, anything
     * else will remove the animation.
     *
     * If this is used on a transparent block, weird graphical effects
     * may occur
     *
     * Note that in order for players to see the changes, you must
     * send them the returned packet
     * @see me.deecaad.compatibility.ICompatibility#sendPackets(Player, Object...)
     *
     * @param block The block to crack
     * @param crack The amount to crack
     * @param id The id to use for the packet
     * @return The constructed packet
     */
    Object getCrackPacket(@Nonnull Block block, int crack, int id);

    /**
     * Gets a <code>PacketPlayOutBlockChange</code> packet for the given
     * <code>bukkitBlock</code> with the given <code>mask</code>. This
     * effectively masks the block as a different block.
     *
     * Note: You probably want to save the previous block state of the block
     * so you can tell the client what the block actually is later. This may
     * cause issues with anti-cheats
     *
     * @param bukkitBlock The bukkit block to mask
     * @param mask The material to change the block to
     * @param data The data (for legacy minecraft) of the material
     * @return Instantiated packet
     */
    Object getBlockMaskPacket(Block bukkitBlock, Material mask, byte data);

    /**
     * Gets a <code>PacketPlayOutBlockChange</code> packet for the given
     * <code>bukkitBlock</code> with the given <code>mask</code>. This
     * effectively masks the block as a different block.
     *
     * Note: You probably want to save the previous block state of the block
     * so you can tell the client what the block actually is later. This may
     * cause issues with anti-cheats
     *
     * @param bukkitBlock The bukkit block to mask
     * @param mask The state to set the block to
     * @return Instantiated packet
     */
    Object getBlockMaskPacket(Block bukkitBlock, BlockState mask);

    /**
     * Gets a <code>PacketPlayOutMultiBlockChange</code> packet holding
     * masks for all of the given <code>blocks</code>. The mask applied
     * depends on <code>mask</code> and <code>data</code> (Though
     * <code>data</code> isn't used in legacy (pre1.13) versions)
     *
     * Note: You probably want to send this packet twice, once to mask the
     * blocks and another time (Which different masks) to unmask the blocks
     *
     * @param blocks The blocks to mask
     * @param mask The material mask
     * @param data The data (for legacy minecraft) of the material
     * @return Instantiated packet
     */
    List<Object> getMultiBlockMaskPacket(List<Block> blocks, Material mask, byte data);

    /**
     * Gets a <code>PacketPlayOutMultiBlockChange</code> packet holding
     * masks for all of the given <code>blocks</code>. The mask applied
     * depends on <code>mask</code>.
     *
     * Note: You probably want to send this packet twice, once to mask the
     * blocks and another time (Which different masks) to unmask the blocks
     *
     * @param blocks The blocks to mask
     * @param mask The state to set as the mask
     * @return Instantiated packet
     */
    List<Object> getMultiBlockMaskPacket(List<Block> blocks, BlockState mask);
}