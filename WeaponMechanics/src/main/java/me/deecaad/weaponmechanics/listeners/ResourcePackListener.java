package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (WeaponMechanics.getBasicConfigurations().getBool("Resource_Pack_Download.Automatically_Send_To_Player")) {
            String link = WeaponMechanics.getBasicConfigurations().getString("Resource_Pack_Download.Link");
            if (link == null || link.isEmpty()) {
                WeaponMechanics.debug.warn("Resource_Pack_Download Link was missing in the config.yml!",
                        "If you don't want to send players the resource pack, please disable Automatically_Send_To_Player instead!");
                return;
            }

            player.setResourcePack(link);
        }
    }

    @EventHandler
    public void onPack(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();

        if (WeaponMechanics.getBasicConfigurations().getBool("Resource_Pack_Download.Force_Player_Download")) {
            PlayerResourcePackStatusEvent.Status status = event.getStatus();

            if (status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD
                    || status == PlayerResourcePackStatusEvent.Status.DECLINED) {

                // TODO consider adding a permission to allow people to be exempt
                String message = WeaponMechanics.getBasicConfigurations().getString("Resource_Pack_Download.Kick_Message");
                player.kickPlayer(StringUtil.color(message));
            }
        }
    }
}
