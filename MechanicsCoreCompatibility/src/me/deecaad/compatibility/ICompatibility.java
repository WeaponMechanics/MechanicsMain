package me.deecaad.compatibility;

import me.deecaad.compatibility.block.BlockCompatibility;
import me.deecaad.compatibility.entity.EntityCompatibility;
import me.deecaad.compatibility.item.nbt.INBTCompatibility;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * The interface used to add compatibility for multiple versions
 */
public interface ICompatibility {

    /**
     * @return true if server version is NOT fully supported
     */
    boolean isNotFullySupported();

    /**
     * Simple method to get player's ping.
     * This is updated automatically and does not require async call.
     *
     * @param player the player instance
     * @return the ping of player in ms
     */
    int getPing(Player player);

    /**
     * This is very useful method when using packet listeners since most of packets use entity ids
     *
     * @param world the world where entity is
     * @param entityId the entity's id
     * @return the entity with that id as bukkit entity or null
     */

    Entity getEntityById(World world, int entityId);

    /**
     * Send all given packet objects to player
     *
     * @param player the player to receive
     * @param packets the packet objects to send
     */
    void sendPackets(Player player, Object... packets);

    /**
     * This will return null ONLY if server versions is 1.13 R2 or above.
     * This is like this because API was added for item NBT tags also in 1.13 R2.
     *
     * @return the NBT compatibility
     */
    @Nonnull
    INBTCompatibility getNBTCompatibility();

    @Nonnull
    EntityCompatibility getEntityCompatibility();

    @Nonnull
    BlockCompatibility getBlockCompatibility();
}
