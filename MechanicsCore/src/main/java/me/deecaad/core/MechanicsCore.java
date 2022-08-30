package me.deecaad.core;

import me.deecaad.core.events.QueueSerializerEvent;
import me.deecaad.core.events.triggers.EquipListener;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerInstancer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.listeners.ItemCraftListener;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.ReflectionUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
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
            FileUtil.copyResourcesTo(getClassLoader().getResource("MechanicsCore"), getDataFolder().toPath());
        }
        FileUtil.ensureDefaults(getClassLoader(), "MechanicsCore/config.yml", new File(getDataFolder(), "config.yml"));

        // The methods we use that allow EntityEquipmentEvent to trigger simply
        // don't exist in 1.10 and lower.
        if (ReflectionUtil.getMCVersion() >= 11) {
            Bukkit.getPluginManager().registerEvents(EquipListener.SINGLETON, this);
        }
        Bukkit.getPluginManager().registerEvents(new ItemCraftListener(), this);

        // Adventure Chat API
        adventure = BukkitAudiences.create(this);
        message = MiniMessage.miniMessage();

        // Handle MechanicsCore custom item registry (You can get items using
        // `/mechanicscore item`).
        File itemsFolder = new File(getDataFolder(), "Items");
        if (!itemsFolder.exists()) {
            FileUtil.copyResourcesTo(getClassLoader().getResource("MechanicsCore/Items"), itemsFolder.toPath());
        }

        for (File file : itemsFolder.listFiles()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            for (String key : config.getKeys(false)) {
                SerializeData data = new SerializeData(new ItemSerializer(), file, key, config);

                try {
                    ItemStack registry = data.of().serializeNonStandardSerializer(new ItemSerializer());
                    ItemSerializer.ITEM_REGISTRY.put(key, registry::clone);
                } catch (SerializerException ex) {
                    ex.log(debug);
                }
            }
        }

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
