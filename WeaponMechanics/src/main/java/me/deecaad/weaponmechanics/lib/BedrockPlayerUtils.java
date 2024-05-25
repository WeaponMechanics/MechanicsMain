package me.deecaad.weaponmechanics.lib;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public class BedrockPlayerUtils {

    private FloodgateApi api;
    private boolean floodgateInstalled = false;

    public BedrockPlayerUtils() {
        if(Bukkit.getPluginManager().getPlugin("floodgate") != null) {
            this.api = FloodgateApi.getInstance();
            this.floodgateInstalled = true;
        } else {
            this.api = null;
            this.floodgateInstalled = false;
        }
    }

    public boolean isFloodgateInstalled() {
        return this.floodgateInstalled;
    }

    public boolean isPlayerBedrock(Player player) {
        return this.floodgateInstalled && this.api.isFloodgatePlayer(player.getUniqueId());
    }

}
