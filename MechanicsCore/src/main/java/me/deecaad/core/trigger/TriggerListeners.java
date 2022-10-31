package me.deecaad.core.trigger;

import me.deecaad.core.MechanicsCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class TriggerListeners {

    private static final FileConfiguration config = MechanicsCore.getPlugin().getConfig();

    @EventHandler(ignoreCancelled = true)
    public void toggleSneak(PlayerToggleSneakEvent e) {
        if (config.getBoolean("Disabled_Trigger_Checks.Sneak")) return;

        Player player = e.getPlayer();
        boolean isSneaking = e.isSneaking();

        weaponHandler.useTrigger(player, isSneaking ? TriggerType.START_SNEAK : TriggerType.END_SNEAK, false);
    }
}
