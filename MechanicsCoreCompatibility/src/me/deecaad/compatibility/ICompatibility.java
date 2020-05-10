package me.deecaad.compatibility;

import me.deecaad.compatibility.nbt.INBTCompatibility;
import me.deecaad.compatibility.projectile.IProjectileCompatibility;
import me.deecaad.compatibility.scope.IScopeCompatibility;
import me.deecaad.compatibility.shoot.IShootCompatibility;
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
     * @return the scope compatibility
     */
    @Nonnull
    IScopeCompatibility getScopeCompatibility();

    /**
     * This will return null ONLY if server versions is 1.13 R2 or above.
     * This is like this because API was added for item NBT tags also in 1.13 R2.
     *
     * @return the NBT compatibility
     */
    @Nonnull
    INBTCompatibility getNBTCompatibility();

    /**
     * @return the projectile compatibility
     */
    @Nonnull
    IProjectileCompatibility getProjectileCompatibility();

    /**
     * @return the shoot compatibility
     */
    @Nonnull
    IShootCompatibility getShootCompatibility();
}
