package me.deecaad.core;

import me.deecaad.core.events.QueueSerializerEvent;
import me.deecaad.core.events.triggers.EquipListener;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerInstancer;
import me.deecaad.core.listeners.ItemCraftListener;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.ReflectionUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.jar.JarFile;

public class MechanicsCore extends JavaPlugin {

    private static MechanicsCore plugin;
    public static Debugger debug; // public for import

    public BukkitAudiences adventure;
    public MiniMessage message;

    @Override
    public void onLoad() {
        int level = getConfig().getInt("Debug_Level");
        boolean printTraces = getConfig().getBoolean("Print_Traces");
        debug = new Debugger(getLogger(), level, printTraces);
        plugin = this;
    }

    @Override
    public void onEnable() {
        debug.debug("Loading config.yml");
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0) {
            try {
                FileUtil.copyResourcesTo(getClassLoader().getResource("MechanicsCore"), getDataFolder().toPath());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        FileUtil.ensureDefaults(getClassLoader(), "MechanicsCore/config.yml", new File(getDataFolder(), "config.yml"));

        // The methods we use that allow EntityEquipmentEvent to trigger simply
        // don't exist in 1.10 and lower.
        if (ReflectionUtil.getMCVersion() >= 11) {
            Bukkit.getPluginManager().registerEvents(EquipListener.SINGLETON, this);
        }
        Bukkit.getPluginManager().registerEvents(new ItemCraftListener(), this);

        adventure = BukkitAudiences.create(this);
        if (ReflectionUtil.getMCVersion() >= 13) {
            MechanicsCoreCommand.build();
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQueue(QueueSerializerEvent event) throws IOException {
                List<Serializer<?>> serializers = new SerializerInstancer(new JarFile(getFile())).createAllInstances(getClassLoader());
                event.addSerializers(serializers);
            }
        }, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
        PlaceholderAPI.onDisable();
        plugin = null;
        debug = null;
        adventure.close();
        adventure = null;
    }

    /**
     * @return the MechanicsCore plugin instance
     */
    public static MechanicsCore getPlugin() {
        return plugin;
    }
}
