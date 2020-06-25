package me.deecaad.core;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.packetlistener.PacketListenerAPI;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MechanicsCore extends JavaPlugin implements Listener {

    private static MechanicsCore plugin;

    // public so people can import a static variable
    public static Debugger debug;

    private List<Serializer<?>> defaultSerializers;

    @Override
    public void onEnable() {

        plugin = this;
        debug = new Debugger(getLogger(), 2, true);
        defaultSerializers = new ArrayList<>();
        new PacketListenerAPI(this);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        PlaceholderAPI.onDisable();
        PacketListenerAPI.onDisable();
        plugin = null;
    }

    /**
     * @return the MechanicsCore plugin instance
     */
    public static MechanicsCore getPlugin() {
        return plugin;
    }

    @EventHandler
    public void testDeleteMe(AsyncPlayerChatEvent e) {
        e.setMessage(StringUtils.color(e.getMessage()));
    }
}
