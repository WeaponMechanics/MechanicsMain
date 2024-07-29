package me.deecaad.core;

import com.cjcrafter.scheduler.SchedulerCompatibility;
import com.cjcrafter.scheduler.ServerImplementation;
import me.deecaad.core.events.QueueSerializerEvent;
import me.deecaad.core.events.triggers.EquipListener;
import me.deecaad.core.file.JarSearcher;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerInstancer;
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
import me.deecaad.core.utils.MinecraftVersions;
import me.deecaad.core.utils.ReflectionUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

public class MechanicsCore extends JavaPlugin {

    private static MechanicsCore instance;
    public static Debugger debug; // public for import

    public BukkitAudiences adventure;
    public MiniMessage message;
    public ServerImplementation foliaScheduler;
    private boolean registeredMechanics;

    public void onLoad() {
        instance = this;
        foliaScheduler = new SchedulerCompatibility(this).getScheduler();

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

                // Sculk methods were added in 1.20.2
                if (MinecraftVersions.TRAILS_AND_TAILS.get(2).isAtLeast()) {
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

        Bukkit.getPluginManager().registerEvents(EquipListener.SINGLETON, this);
        Bukkit.getPluginManager().registerEvents(new ItemCraftListener(), this);
        Bukkit.getPluginManager().registerEvents(new MechanicsCastListener(), this);

        // Adventure Chat API
        adventure = BukkitAudiences.create(this);
        message = MiniMessage.miniMessage();

        // Handle MechanicsCore custom item registry (You can get items using
        // `/mechanicscore item`).
        File itemsFolder = new File(getDataFolder(), "Items");
        if (itemsFolder.exists()) {
            debug.error("Found Items folder... This feature is no longer supported. Please remove the Items folder.");
        }

        if (MinecraftVersions.UPDATE_AQUATIC.isAtLeast()) {
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
        debug = null;
        adventure.close();
        adventure = null;
    }

    public @NotNull ServerImplementation getFoliaScheduler() {
        return foliaScheduler;
    }

    /**
     * @return the MechanicsCore plugin instance
     */
    public static MechanicsCore getPlugin() {
        return instance;
    }
}
