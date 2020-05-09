package me.deecaad.core;

import me.deecaad.core.packetlistener.PacketListenerAPI;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class MechanicsCore extends JavaPlugin {

    public static Debugger debug;
    public static MechanicsCore instance;

    @Override
    public void onLoad() {
        int level = getConfig().getInt("Debug_Level", 2);
        debug = new Debugger(getLogger(), level);
    }

    @Override
    public void onEnable() {
        new PacketListenerAPI(this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);

        PacketListenerAPI.onDisable();
        PlaceholderAPI.onDisable();
    }
}
