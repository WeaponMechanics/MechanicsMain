package me.deecaad.core.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class encapsulates the idea of a visual
 * effect. Visual effects should be displayed to
 * all users within the effect's viewable distance.
 */
public interface Effect extends Repeatable, Delayable {

    double VIEW_DISTANCE = 25.0;

    /**
     * Spawns this effect at the given location.
     * No data is given to the effect
     *
     * @param loc The location to spawn at
     */
    default void spawn(@Nonnull Plugin source, @Nonnull Location loc) {
        if (loc.getWorld() == null) throw new IllegalArgumentException("World cannot be null");

        spawn(source, loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), null);
    }

    /**
     * Spawns this effect in the given world at
     * the given coordinates. No data is given
     * to the effect.
     *
     * @param world Which world to spawn in
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    default void spawn(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z) {
        spawn(source, world, x, y, z, null);
    }

    /**
     * Spawns this effect at the given location with
     * the given data
     *
     * @param loc Location to spawn the effect
     * @param data Data to give to the handler
     */
    default void spawn(@Nonnull Plugin source, @Nonnull Location loc, @Nullable Object data) {
        if (loc.getWorld() == null) throw new IllegalArgumentException("World cannot be null");

        spawn(source, loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), data);
    }

    /**
     * Spawns this effect in the given world at the
     * given coordinates with the given data.
     *
     * Takes <code>Repeatable</code> data and
     * <code>Delayable</code> data into effect
     * @see Repeatable
     * @see Delayable
     *
     * @param world Which world to spawn in
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param data Data to give to the handler
     */
    default void spawn(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        Bukkit.getScheduler().runTaskLater(source, () -> {
            for (int i = 0; i < getRepeatAmount(); i++) {
                Bukkit.getScheduler().runTaskLater(source, () -> {
                    spawnOnce(source, world, x, y, z, data);
                }, i * getRepeatInterval());
            }
        }, getDelay());
    }

    /**
     * Spawns this effect in the given world at the
     * given coordinates with the given data once.
     * This method should never take any data from
     * <code>Repeatable</code> into effect
     *
     * @param world Which world to spawn in
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param data Data to give to the handler
     */
    void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data);
}
