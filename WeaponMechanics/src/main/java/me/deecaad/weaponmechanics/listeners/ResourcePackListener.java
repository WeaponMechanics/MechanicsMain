package me.deecaad.weaponmechanics.listeners;

import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ResourcePackListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (WeaponMechanics.getBasicConfigurations().getBool("Resource_Pack_Download.Automatically_Send_To_Player")) {
            player.setResourcePack(WeaponMechanics.getBasicConfigurations().getString("Resource_Pack_Download.Link"));
        }
    }
}
