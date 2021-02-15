package me.deecaad.compatibility;

import me.deecaad.compatibility.block.BlockCompatibility;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.compatibility.nbt.NBTCompatibility;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * This interface outlines a version dependant api, where there is an
 * implementing class for each minecraft protocol version.
 */
public interface ICompatibility {

    /**
     * Returns <code>true</code> if the minecraft protocol version that the
     * server is running is supported by the implementing class.
     *
     * @implNote
     * The default implementation will return <code>true</code> if
     * {@link Reflection} is used.
     *
     * @return <code>true</code> if the version is not fully supported.
     */
    boolean isNotFullySupported();

    /**
     * Returns the player's ping, or the time, in milliseconds, that it takes
     * for a packet to be sent/received for the <code>player</code>. This
     * method is most likely spoofable, meaning that hacked clients can
     * <i>lie</i> about this number.
     *
     * @param player The non-null player to get the ping of.
     * @return The ping, in milliseconds, of the player.
     */
    int getPing(@Nonnull Player player);

    /**
     * Returns the bukkit {@link Entity} whose handle's unique id matches the
     * given <code>entityId</code>. Ids are unique to the {@link World}.
     *
     * @param world    The non-null bukkit world that holds the entity.
     * @param entityId The unique, numeric id of the entity.
     * @return The bukkit entity with the id, or <code>null</code>.
     */
    Entity getEntityById(@Nonnull World world, int entityId);

    /**
     * Overloaded version of {@link #sendPackets(Player, Object...)} which does
     * not need to instantiate a new array of packets every time 1 packet needs
     * to be sent.
     *
     * @param player The non-null player to send the packet to.
     * @param packet The non-null packet to send to the player.
     */
    void sendPackets(Player player, Object packet);

    /**
     * Sends the given <code>packets</code> to the given <code>player</code>.
     * This can be run asynchronously
     *
     * @param player  The non-null player to send the packet to.
     * @param packets The non-null array of non-null packets to send to the
     *                player.
     */
    void sendPackets(Player player, Object... packets);

    /**
     * Returns this version's loaded {@link NBTCompatibility}. The classes for
     * each version can be found in the nbt package
     * ({@link me.deecaad.compatibility.nbt}).
     *
     * @return This version's non-null nbt compatibility.
     * @throws UnsupportedOperationException In minecraft protocol versions
     *                                       1_13_R2 and higher.
     */
    @Nonnull
    NBTCompatibility getNBTCompatibility();

    /**
     * Returns this version's loaded {@link EntityCompatibility}. The classes
     * for each version can be found in the entity package
     * ({@link me.deecaad.compatibility.entity}).
     *
     * @return This version's non-null entity compatibility.
     */
    @Nonnull
    EntityCompatibility getEntityCompatibility();

    /**
     * Returns this version's loaded {@link BlockCompatibility}. The classes
     * for each version can be found in the block package
     * ({@link me.deecaad.compatibility.block}).
     *
     * @return This version's non-null block compatibility.
     */
    @Nonnull
    BlockCompatibility getBlockCompatibility();
}
