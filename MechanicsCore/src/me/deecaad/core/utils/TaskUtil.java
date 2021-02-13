package me.deecaad.core.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This final utility class outlines static methods that simplify the async
 * task to sync callback structure.
 */
public final class TaskUtil {
    
    // Don't let anyone instantiate this class.
    private TaskUtil() { }

    /**
     * Shorthand for using {@link #runAsync(Plugin, IAsync, ICallback)} with a
     * <code>null</code> callback. Using this method is the same thing has
     * running a {@link BukkitRunnable} async.
     *
     * @param plugin The non-null plugin scheduling the async task.
     * @param async  The non-null task to run async.
     */
    public static void runAsync(@Nonnull Plugin plugin, @Nonnull IAsync async) {
        runAsync(plugin, async, null);
    }

    public static void runAsync(@Nonnull Plugin plugin, @Nonnull IAsync async, @Nullable ICallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Object value = async.execute();
                if (callback == null || value == null) {
                    return;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.execute(value);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Runs callback task in sync with server thread
     *
     * @param plugin the plugin instance used to run task
     * @param callback the callback ran in sync
     */
    public static void runSync(Plugin plugin, ICallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                callback.execute(null);
            }
        }.runTask(plugin);
    }
}