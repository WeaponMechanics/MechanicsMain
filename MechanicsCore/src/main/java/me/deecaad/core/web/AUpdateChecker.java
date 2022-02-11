package me.deecaad.core.web;

import me.deecaad.core.utils.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class AUpdateChecker implements Listener {

    private final SpigotResource spigotResource;
    private final int requiredMajorVersionsBehind;
    private final int requiredMinorVersionsBehind;
    private final int requiredPatchVersionsBehind;

    public AUpdateChecker(SpigotResource spigotResource, int requiredMajorVersionsBehind, int requiredMinorVersionsBehind, int requiredPatchVersionsBehind) {
        this.spigotResource = spigotResource;
        this.requiredMajorVersionsBehind = requiredMajorVersionsBehind;
        this.requiredMinorVersionsBehind = requiredMinorVersionsBehind;
        this.requiredPatchVersionsBehind = requiredPatchVersionsBehind;
        Bukkit.getServer().getPluginManager().registerEvents(this, spigotResource.getPlugin());
        new BukkitRunnable() {
            @Override
            public void run() {
                spigotResource.update();

                if (hasUpdate()) {
                    // Update found -> notify players
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                onUpdateFound(player, spigotResource);
                            }
                        }
                    }.runTask(spigotResource.getPlugin());
                }
            }
        }.runTaskTimerAsynchronously(spigotResource.getPlugin(), 0, NumberUtil.HOUR_IN_TICKS);
    }

    /**
     * @return whether there is update available
     */
    public boolean hasUpdate() {
        return this.requiredMajorVersionsBehind <= spigotResource.getMajorVersionsBehind()
                || this.requiredMinorVersionsBehind <= spigotResource.getMinorVersionsBehind()
                || this.requiredPatchVersionsBehind <= spigotResource.getPatchVersionsBehind();
    }

    /**
     * @return the spigot resource used with this update checker
     */
    public SpigotResource getSpigotResource() {
        return spigotResource;
    }

    /**
     * This is ran for all players online when update is found or player joins server
     *
     * @param sender the sender instance (or player)
     * @param spigotResource the spigot resource instance
     */
    public abstract void onUpdateFound(CommandSender sender, SpigotResource spigotResource);

    @EventHandler
    public void join(PlayerJoinEvent e) {
        if (!hasUpdate()) return;
        onUpdateFound(e.getPlayer(), spigotResource);
    }
}