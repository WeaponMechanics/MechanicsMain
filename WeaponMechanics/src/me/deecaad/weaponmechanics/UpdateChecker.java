package me.deecaad.weaponmechanics;

import me.deecaad.core.web.AUpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class UpdateChecker extends AUpdateChecker {

    public UpdateChecker(Plugin plugin, String resourceID, int requiredVersionsBehind) {
        super(plugin, resourceID, requiredVersionsBehind);
    }

    @Override
    public void whenUpdateIsFound() {
        Bukkit.getOnlinePlayers().forEach(this::whenPlayerJoinsAndUpdateIsFound);
    }

    @Override
    public void whenPlayerJoinsAndUpdateIsFound(Player player) {
        if (!player.hasPermission("weaponmechanics.admin")) return;

        double updates = getVersionsBehind();
        // Red if x>=10 updates, yellow if x>3, else green
        char color = updates >= 10 ? 'c': updates > 3 ? 'e' : 'a';
        player.sendMessage("§7➢  §6You are §" + color + updates + "§6 behind");
        player.sendMessage("§7➢  §6Server version: §7" + getResourceCurrentVersion());
        player.sendMessage("§7➢  §6Update version: §7" + getLatestUpdateVersion());
        player.sendMessage("§7➢  §6Download the update at: §7spigot.com/best_plugin_ever");
    }
}
