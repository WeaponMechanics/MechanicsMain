package me.deecaad.compatibility.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;

public interface BlockCompatibility {

    AtomicInteger IDS = new AtomicInteger(0);

    /**
     * Sends a Block Break Animation packet to every player
     * in the block's <code>World</code>.
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
    Object getCrackPacket(Block block, int crack);

    Object createFallingBlock(Block block, Vector vector);

    //todo BlockMask
}
