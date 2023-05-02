package me.deecaad.weaponmechanics.listeners;

import me.cjcrafter.auto.AutoMechanicsDownload;
import me.deecaad.core.file.TaskChain;
import me.deecaad.core.utils.LogLevel;
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

            if (("https://raw.githubusercontent.com/WeaponMechanics/MechanicsMain/master/WeaponMechanicsResourcePack.zip").equals(link)) {
                // So when the server admin uses the default link, we should change
                // the link to find the latest version of the resource pack.
                // Unfortunately, minecraft doesn't automatically download new
                // versions of the pack, so we have to have a unique link in order
                // to force the players to download the most recent pack.
                new TaskChain(WeaponMechanics.getPlugin())
                        .thenRunAsync((data) -> {
                            try {
                                AutoMechanicsDownload auto = new AutoMechanicsDownload(10000, 30000);
                                String version = auto.RESOURCE_PACK_VERSION;
                                return "https://raw.githubusercontent.com/WeaponMechanics/MechanicsMain/master/resourcepack/WeaponMechanicsResourcePack-" + version + ".zip";
                            } catch (InternalError e) {
                                WeaponMechanics.debug.log(LogLevel.DEBUG, "Failed to fetch resource pack version due to timeout", e);
                                return null;
                            }
                        }).thenRunSync((data) -> {
                            if (!player.isOnline() || data == null) return null;

                            WeaponMechanics.debug.debug("Sending " + player.getName() + " resource pack: " + data);
                            player.setResourcePack((String) data);
                            return null;
                        });
                return;
            }
            WeaponMechanics.debug.debug("Sending " + player.getName() + " resource pack: " + link);
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
