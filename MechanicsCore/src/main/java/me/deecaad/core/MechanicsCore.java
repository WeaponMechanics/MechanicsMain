package me.deecaad.core;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.events.triggers.EquipListener;
import me.deecaad.core.file.JarInstancer;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class MechanicsCore extends JavaPlugin {

    private static MechanicsCore plugin;
    private static List<Serializer<?>> serializersList;

    // public so people can import a static variable
    public static Debugger debug;

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
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0)
            FileUtil.copyResourcesTo(getClass(), getClassLoader(), "MechanicsCore", getDataFolder());
        FileUtil.ensureDefaults(getClassLoader(), "MechanicsCore/config.yml", new File(getDataFolder(), "config.yml"));

        try {
            List<?> serializers = new JarInstancer(new JarFile(getFile())).createAllInstances(Serializer.class, getClassLoader(), true);
            //noinspection unchecked
            serializersList = (List<Serializer<?>>) serializers;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // The methods we use that allow EntityEquipmentEvent to trigger simply
        // don't exist in 1.10 and lower.
        if (ReflectionUtil.getMCVersion() >= 11) {
            Bukkit.getPluginManager().registerEvents(EquipListener.SINGLETON, this);
        }
        Bukkit.getPluginManager().registerEvents(new ItemSerializer(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
        PlaceholderAPI.onDisable();
        plugin = null;
        serializersList = null;
        debug = null;
    }

    /**
     * @return the MechanicsCore plugin instance
     */
    public static MechanicsCore getPlugin() {
        return plugin;
    }

    /**
     * @return the list of all serializers added to core
     */
    public static List<Serializer<?>> getListOfSerializers() {
        return new ArrayList<>(serializersList);
    }

    /**
     * Add serializer for MechanicsCore serializer list
     *
     * @param serializer the serializer
     */
    public static void addSerializer(Plugin plugin, Serializer<?> serializer) {
        if (serializersList == null) {
            debug.log(LogLevel.WARN, plugin.getName() + " tried to add serializer after startup...");
            return;
        }
        serializersList.add(serializer);
    }

    /**
     * Add list of serializers for MechanicsCore serializer list
     *
     * @param serializers the list of serializers
     */
    public static void addSerializers(Plugin plugin, List<Serializer<?>> serializers) {
        if (serializers != null && serializers.size() > 0) {
            for (Serializer<?> serializer : serializers) {
                addSerializer(plugin, serializer);
            }
        }
    }
}
