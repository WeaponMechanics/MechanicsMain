package me.deecaad.core.web;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class AUpdateChecker extends SpigotResource implements Listener {

    private double requiredVersionsBehind;
    private int taskID;

    private int versionsBehind;

    public AUpdateChecker(Plugin plugin, String resourceID, int requiredVersionsBehind) {
        super(plugin, resourceID);
        this.versionsBehind = 0;
        this.requiredVersionsBehind = requiredVersionsBehind * 0.01;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Checks if update is available for given resource id compared to plugin given.
     * SpigotResource should be updated before calling this.
     * Remember to run this asynchronously.
     */
    public void checkUpdateAvailable() {
        String latestUpdateVersion = getLatestUpdateVersion();
        if (latestUpdateVersion.equals("Unknown")) {
            return;
        }
        double remote = Double.parseDouble(latestUpdateVersion.split(" ")[0]);
        double local = Double.parseDouble(getResourceCurrentVersion().split(" ")[0]);

        if (remote > local) {
            double difference = remote - local;
            if (difference >= requiredVersionsBehind) {
                versionsBehind = (int) (difference * 100);
            }
        }
    }

    /**
     * Method checkUpdateAvailable() in this class should be ran before using this.
     *
     * @return true if update is available
     */
    public boolean hasUpdateAvailable() {
        return versionsBehind != 0;
    }

    /**
     * Method checkUpdateAvailable() in this class should be ran before using this.
     *
     * @return the amount of versions behind or 0 if no available updates
     */
    public int getVersionsBehind() {
        return versionsBehind;
    }

    /**
     * Starts the update checking task if not yet started.
     * Updates will be checked every hour once (72000 ticks).
     *
     * @param plugin the plugin instance used to run task
     */
    public void startCheckForUpdatesTask(Plugin plugin) {
        if (hasUpdateAvailable() || this.taskID != 0) {
            return;
        }
        this.taskID = new BukkitRunnable() {
            @Override
            public void run() {
                updateVersion();
                checkUpdateAvailable();
                if (!hasUpdateAvailable()) {
                    return;
                }
                updateDescription(true, true);
                AUpdateChecker.this.taskID = 0;
                cancel();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        whenUpdateIsFound();
                    }
                }.runTask(plugin);
            }
        }.runTaskTimerAsynchronously(plugin, 200, 72000).getTaskId();
    }

    /**
     * Returns 0 if update checker task isn't running, otherwise this returns the runnable's task id
     *
     * @return the update checker task id
     */
    public int getTaskID() {
        return this.taskID;
    }

    /**
     * This will be ran when update is found.
     * Update checker task is cancelled automatically before this is ran.
     * Update version, title and description are also automatically updated before this is ran.
     */
    public abstract void whenUpdateIsFound();

    /**
     * This will be ran when update is found and player joins server.
     */
    public abstract void whenPlayerJoinsAndUpdateIsFound(Player player);

    @EventHandler
    public void join(PlayerJoinEvent e) {
        if (!hasUpdateAvailable()) {
            return;
        }
        whenPlayerJoinsAndUpdateIsFound(e.getPlayer());
    }
}