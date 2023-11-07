package me.deecaad.core;

import me.deecaad.core.events.QueueSerializerEvent;
import me.deecaad.core.events.triggers.EquipListener;
import me.deecaad.core.file.*;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.listeners.ItemCraftListener;
import me.deecaad.core.listeners.MechanicsCastListener;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.conditions.GeyserCondition;
import me.deecaad.core.mechanics.conditions.MythicMobsEntityCondition;
import me.deecaad.core.mechanics.conditions.MythicMobsFactionCondition;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.defaultmechanics.MythicSkillMechanic;
import me.deecaad.core.mechanics.defaultmechanics.SculkBloomMechanic;
import me.deecaad.core.mechanics.defaultmechanics.SculkShriekMechanic;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
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
import java.util.*;
import java.util.jar.JarFile;

public class MechanicsCore extends JavaPlugin {

    private static MechanicsCore instance;
    public static Debugger debug; // public for import

    public BukkitAudiences adventure;
    public MiniMessage message;
    private boolean registeredMechanics;

    public void onLoad() {
        instance = this;

        int level = getConfig().getInt("Debug_Level");
        boolean printTraces = getConfig().getBoolean("Print_Traces");
        debug = new Debugger(getLogger(), level, printTraces);

        // Search the jar file for Mechanics, Targeters, and Conditions. We
        // need to register them to the Mechanics.class registries.
        if (!registeredMechanics) {
            registeredMechanics = true;
            try {
                JarSearcher searcher = new JarSearcher(new JarFile(getFile()));

                searcher.findAllSubclasses(Mechanic.class, getClassLoader(), true)
                        .stream().map(ReflectionUtil::newInstance).forEach(Mechanics.MECHANICS::add);
                searcher.findAllSubclasses(Targeter.class, getClassLoader(), true)
                        .stream().map(ReflectionUtil::newInstance).forEach(Mechanics.TARGETERS::add);
                searcher.findAllSubclasses(Condition.class, getClassLoader(), true)
                        .stream().map(ReflectionUtil::newInstance).forEach(Mechanics.CONDITIONS::add);

                // Sculk methods were added in 1.20.1
                if (ReflectionUtil.getMCVersion() >= 20) {
                    Mechanics.MECHANICS.add(new SculkShriekMechanic());
                    Mechanics.MECHANICS.add(new SculkBloomMechanic());
                }

                try {
                    // Add the MythicMobs conditions ONLY IF mythicmobs is present to avoid errors
                    if (getServer().getPluginManager().getPlugin("MythicMobs") != null) {
                        Mechanics.MECHANICS.add(new MythicSkillMechanic());
                        Mechanics.CONDITIONS.add(new MythicMobsEntityCondition());
                        Mechanics.CONDITIONS.add(new MythicMobsFactionCondition());
                    }
                } catch (Throwable ex) {
                    debug.warn("Cannot hook into MythicMobs... MythicMobs might be outdated");
                }

                try {
                    if (getServer().getPluginManager().isPluginEnabled("Geyser-Spigot")) {
                        Mechanics.CONDITIONS.add(new GeyserCondition());
                    }
                } catch (Throwable ex) {
                    debug.warn("Cannot hook into Geyser... Geyser might be outdated");
                }

                // Placeholders
                searcher.findAllSubclasses(PlaceholderHandler.class, getClassLoader(), true)
                        .stream().map(ReflectionUtil::newInstance).forEach(PlaceholderHandler.REGISTRY::add);

            } catch (IOException ex) {
                debug.log(LogLevel.ERROR, "Error while searching Jar", ex);
            }
        }
    }

    public void onEnable() {
        debug.debug("Loading config.yml");
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0) {
            FileUtil.copyResourcesTo(getClassLoader().getResource("MechanicsCore"), getDataFolder().toPath());
        }
        FileUtil.ensureDefaults(getClassLoader().getResource("MechanicsCore/config.yml"), new File(getDataFolder(), "config.yml"));

        // The methods we use that allow EntityEquipmentEvent to trigger simply
        // don't exist in 1.10 and lower.
        if (ReflectionUtil.getMCVersion() >= 11) {
            Bukkit.getPluginManager().registerEvents(EquipListener.SINGLETON, this);
        }
        Bukkit.getPluginManager().registerEvents(new ItemCraftListener(), this);
        Bukkit.getPluginManager().registerEvents(new MechanicsCastListener(), this);

        // Adventure Chat API
        adventure = BukkitAudiences.create(this);
        message = MiniMessage.miniMessage();

        // Handle MechanicsCore custom item registry (You can get items using
        // `/mechanicscore item`).
        File itemsFolder = new File(getDataFolder(), "Items");
        if (!itemsFolder.exists()) {
            FileUtil.copyResourcesTo(getClassLoader().getResource("MechanicsCore/Items"), itemsFolder.toPath());
        }

        int registeredCount = loadItems(itemsFolder);
        debug.info("Registered " + registeredCount + " custom items from " + itemsFolder);

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

    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getServer().getScheduler().cancelTasks(this);
        debug = null;
        adventure.close();
        adventure = null;
    }

    public int loadItems(File directory) {
        int loadLimit = getConfig().getInt("Item_Loop_Limit", 10);
        List<String> registered = new ArrayList<>();
        while (--loadLimit > 0) {
            registered.addAll(loadItems(directory, registered, loadLimit == 1));
        }
        return registered.size();
    }

    public List<String> loadItems(File directory, List<String> excludes, boolean isError) {
        if (!directory.isDirectory())
            throw new IllegalArgumentException(directory + " is not a directory");

        List<String> added = new ArrayList<>();
        for (File file : directory.listFiles()) {

            // Allow sub-folders
            if (file.isDirectory()) {
                Set<String> temp = new HashSet<>(excludes);
                temp.addAll(added);

                added.addAll(loadItems(file, new ArrayList<>(temp), isError));
                continue;
            }

            // Quick check to see if somebody added a file type that isn't a
            // YAML file. Otherwise we might get a stacktrace in console
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".yml") && !file.getName().toLowerCase(Locale.ROOT).endsWith(".yaml")) {
                debug.error(file + " was not a YAML file? Make sure it ends with .yml");
                continue;
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false)) {

                // The item was already added
                if (excludes.contains(key))
                    continue;

                SerializeData data = new SerializeData(new ItemSerializer(), file, key, new BukkitConfig(config));

                try {
                    ItemStack registry = data.of().serialize(new ItemSerializer());
                    ItemSerializer.ITEM_REGISTRY.put(key, registry::clone);
                    added.add(key);
                } catch (SerializerException ex) {

                    if (!isError) {
                        // At this point, we're not quite sure if this is an error
                        // or not. Since custom item loading order is undefined, we
                        // simply run this a few times and keep our fingers crossed
                        // that the admin hasn't nested their files horribly.
                        ex.addMessage("This is a debug message, and can be ignored. Check below for actual errors.");
                        ex.log(debug, LogLevel.DEBUG);
                    } else {
                        ex.addMessage("Failed to load item in " + getConfig().getInt("Item_Loop_Limit", 10) + " attempts");
                        ex.log(debug);
                    }

                    debug.log(LogLevel.DEBUG, "", ex);
                }
            }
        }
        return added;
    }


    /**
     * @return the MechanicsCore plugin instance
     */
    public static MechanicsCore getPlugin() {
        return instance;
    }
}
