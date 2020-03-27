package me.deecaad.core.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncUtil {
    
    /**
     * Don't let anyone instantiate this class
     */
    private AsyncUtil() {
    }
    
    /**
     * Runs task asynchronously.
     *
     * @param plugin the plugin instance used to run task
     * @param async the async execution
     */
    public static void runAsync(Plugin plugin, IAsync async) {
        runAsync(plugin, async, null);
    }

    /**
     * Runs task asynchronously with callback to synchronized task.
     * If async value is null, then callback wont be ran
     *
     * @param plugin the plugin instance used to run task
     * @param async the async execution
     * @param callback the callback ran in sync
     */
    public static void runAsync(Plugin plugin, IAsync async, ICallback callback) {
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