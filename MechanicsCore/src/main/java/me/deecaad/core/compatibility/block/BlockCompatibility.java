package me.deecaad.core.compatibility.block;

import me.deecaad.core.compatibility.ICompatibility;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
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
            case FALL -> sounds.getFallSound();
        };

        return soundData;
    }

    /**
     * Returns a positive float representing the blast material of a given block. Materials with a
     * higher blast resistance are less likely to be destroyed by explosions.
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
        BREAK,
        STEP,
        PLACE,
        HIT,
        FALL
    }
}