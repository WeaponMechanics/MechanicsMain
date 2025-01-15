package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.ICompatibility;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This interface outlines a version dependant api that return values based on different
 * {@link Block} inputs. There should be an implementing class for each minecraft protocol version.
 *
 * <p>
 * For methods that return packets, in order for those packets to be visible to players, the packets
 * need to be sent to the players. See {@link ICompatibility#sendPackets(Player, Object)}.
 */
public interface BlockCompatibility {

    /**
     * Threadsafe method to get unique ids for block cracking.
     *
     * @see #getCrackPacket(Block, int)
     */
    AtomicInteger IDS = new AtomicInteger(0);

    /**
     * Returns a block break animation packet for the given <code>block</code> and <code>crack</code>.
     * This should probably not be used for transparent blocks. This method is a shorthand for
     * {@link #getCrackPacket(Block, int, int)}.
     *
     * <p>
     * For more information, please see the protocol
     * <a href="https://wiki.vg/Protocol#Block_Break_Animation">wiki</a>.
     *
     * @param block The non-null block to display the cracking animation over.
     * @param crack The cracking amount, between 0 and 9 inclusively. Higher values are more visibly
     *        cracked.
     * @return The non-null animation packet.
     */
    @NotNull Object getCrackPacket(@NotNull Block block, int crack);

    /**
     * Returns a block break animation packet for the given <code>block</code> and <code>crack</code>.
     * This should probably not be used for transparent blocks. The <code>id</code> is a unique to each
     * packet, and sending a new packet with the same id will cause the previous one to be overwritten.
     *
     * <p>
     * For more information, please see the protocol
     * <a href="https://wiki.vg/Protocol#Block_Break_Animation">wiki</a>.
     *
     * @param block The non-null block to display the cracking animation over.
     * @param crack The cracking amount, between 0 and 9 inclusively. Higher values are more visibly
     *        cracked.
     * @param id The unique id. If you do not want to override the previous packet, use
     *        {@link #getCrackPacket(Block, int)}}.
     * @return The non-null animation packet.
     */
    @NotNull Object getCrackPacket(@NotNull Block block, int crack, int id);
}
