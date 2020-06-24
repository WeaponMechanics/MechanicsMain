package me.deecaad.core.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class encapsulates the idea of a visual
 * effect. Visual effects should be displayed to
 * all users within the effect's viewable distance.
 */
public abstract class Effect implements Repeatable, Delayable, Offsetable {

    // todo configurable
    public static double VIEW_DISTANCE = 25.0;

    private int repeatAmount;
    private int repeatInterval;
    private int delay;
    private Vector offset;

    public Effect() {
        repeatAmount = 1;
        offset = new Vector();
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public int getRepeatAmount() {
        return repeatAmount;
    }

    @Override
    public void setRepeatAmount(int repeatAmount) {
        this.repeatAmount = repeatAmount;
    }

    @Override
    public int getRepeatInterval() {
        return repeatInterval;
    }

    @Override
    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    @Override
    public Vector getOffset() {
        return offset;
    }

    @Override
    public void setOffset(Vector offset) {
        this.offset = offset;
    }

    /**
     * Spawns this effect at the given location.
     * No data is given to the effect
     *
     * @param source The plugin spawning the particles. Used for scheduling
     * @param loc The location to spawn at
     */
    public final void spawn(@Nonnull Plugin source, @Nonnull Location loc) {
        if (loc.getWorld() == null) throw new IllegalArgumentException("World cannot be null");

        spawn(source, loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), null);
    }

    /**
     * Spawns this effect in the given world at
     * the given coordinates. No data is given
     * to the effect.
     *
     * @param source The plugin spawning the particles. Used for scheduling
     * @param world Which world to spawn in
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public final void spawn(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z) {
        spawn(source, world, x, y, z, null);
    }

    /**
     * Spawns this effect at the given location with
     * the given data
     *
     * @param source The plugin spawning the particles. Used for scheduling
     * @param loc Location to spawn the effect
     * @param data Data to give to the handler
     */
    public final void spawn(@Nonnull Plugin source, @Nonnull Location loc, @Nullable Object data) {
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
     * @param source The plugin spawning the particles. Used for scheduling
     * @param world Which world to spawn in
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param data Data to give to the handler
     */
    public final void spawn(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        Bukkit.getScheduler().runTaskLater(source, () -> {
            for (int i = 0; i < getRepeatAmount(); i++) {
                Bukkit.getScheduler().runTaskLater(source, () -> {
                    Vector offset = getOffset();
                    spawnOnce(source, world, x + offset.getX(), y + offset.getY(), z + offset.getZ(), data);
                }, i * getRepeatInterval());
            }
        }, getDelay());
    }

    /**
     * Spawns this effect for the given player
     * at the given locatio
     *
     * @param loc The location to spawn at
     */
    public final void spawnFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull Location loc) {
        if (loc.getWorld() == null) throw new IllegalArgumentException("World cannot be null");

        spawnFor(source, player, loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), null);
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
    public final void spawnFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull World world, double x, double y, double z) {
        spawnFor(source, player, world, x, y, z, null);
    }

    /**
     * Spawns this effect at the given location with
     * the given data
     *
     * @param loc Location to spawn the effect
     * @param data Data to give to the handler
     */
    public final void spawnFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull Location loc, @Nullable Object data) {
        if (loc.getWorld() == null) throw new IllegalArgumentException("World cannot be null");

        spawnFor(source, player, loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), data);
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
    public final void spawnFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull World world, double x, double y, double z, @Nullable Object data) {
        Bukkit.getScheduler().runTaskLater(source, () -> {
            for (int i = 0; i < getRepeatAmount(); i++) {
                Bukkit.getScheduler().runTaskLater(source, () -> {
                    Vector offset = getOffset();
                    spawnOnceFor(source, player, world, x + offset.getX(), y + offset.getY(), z + offset.getZ(), data);
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
    protected abstract void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data);

    protected abstract void spawnOnceFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull World world, double x, double y, double z, @Nullable Object data);
}
