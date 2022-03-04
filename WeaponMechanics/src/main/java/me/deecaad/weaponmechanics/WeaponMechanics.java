package me.deecaad.weaponmechanics;

import co.aikar.timings.lib.MCTiming;
import co.aikar.timings.lib.TimingManager;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.worldguard.WorldGuardCompatibility;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.DuplicateKeyException;
import me.deecaad.core.file.FileReader;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.JarInstancer;
import me.deecaad.core.file.LinkedConfig;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerInstancer;
import me.deecaad.core.file.TaskChain;
import me.deecaad.core.packetlistener.PacketHandlerListener;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.web.SpigotResource;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsMainCommand;
import me.deecaad.weaponmechanics.lib.MythicMobsLoader;
import me.deecaad.weaponmechanics.listeners.ExplosionInteractionListeners;
import me.deecaad.weaponmechanics.listeners.ResourcePackListener;
import me.deecaad.weaponmechanics.listeners.WeaponListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerEntityListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerPlayerListeners;
import me.deecaad.weaponmechanics.packetlisteners.OutAbilitiesListener;
import me.deecaad.weaponmechanics.packetlisteners.OutEntityEffectListener;
import me.deecaad.weaponmechanics.packetlisteners.OutRemoveEntityEffectListener;
import me.deecaad.weaponmechanics.packetlisteners.OutSetSlotBobFix;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectilesRunnable;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.error.YAMLException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class WeaponMechanics {

    private static WeaponMechanics plugin;
    WeaponMechanicsLoader javaPlugin;
    Map<LivingEntity, EntityWrapper> entityWrappers;
    Configuration configurations;
    Configuration basicConfiguration;
    MainCommand mainCommand;
    WeaponHandler weaponHandler;
    UpdateChecker updateChecker;
    ProjectilesRunnable projectilesRunnable;
    PacketHandlerListener packetListener;
    TimingManager timingManager;

    // public so people can import a static variable
    public static Debugger debug;

    public WeaponMechanics(WeaponMechanicsLoader javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    public org.bukkit.configuration.Configuration getConfig() {
        return javaPlugin.getConfig();
    }

    public Logger getLogger() {
        return javaPlugin.getLogger();
    }

    public File getDataFolder() {
        return javaPlugin.getDataFolder();
    }

    public ClassLoader getClassLoader() {
        return javaPlugin.getClassLoader0();
    }

    public File getFile() {
        return javaPlugin.getFile0();
    }

    public void onLoad() {
        setupDebugger();

        // Check Java version and warn users about untested/unsupported versions
        if (ReflectionUtil.getJavaVersion() < 8) {
            debug.error("Detected a JAVA version under java 1.8. This plugin will NOT work in versions under java 1.8.");
            debug.error("Detected JAVA version: " + ReflectionUtil.getJavaVersion());
        } else if (ReflectionUtil.getJavaVersion() > 17) {
            debug.debug("Detected a JAVA version above java 17. This plugin has not been tested in versions above java 17.");
            debug.debug("Detected JAVA version: " + ReflectionUtil.getJavaVersion());
        }

        // Register all WorldGuard flags
        WorldGuardCompatibility guard = CompatibilityAPI.getWorldGuardCompatibility();
        if (guard.isInstalled()) {
            debug.info("Detected WorldGuard, registering flags");
            guard.registerFlag("weapon-shoot", WorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-shoot-message", WorldGuardCompatibility.FlagType.STRING_FLAG);
            guard.registerFlag("weapon-explode", WorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-explode-message", WorldGuardCompatibility.FlagType.STRING_FLAG);
            guard.registerFlag("weapon-break-block", WorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-damage", WorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-damage-message", WorldGuardCompatibility.FlagType.STRING_FLAG);
        } else {
            debug.debug("No WorldGuard detected!");
        }
    }

    public void onEnable() {
        long millisCurrent = System.currentTimeMillis();

        plugin = this;
        entityWrappers = new HashMap<>();
        timingManager = TimingManager.of(getPlugin());

        writeFiles();
        registerPacketListeners();

        weaponHandler = new WeaponHandler();

        // Start custom projectile runnable
        projectilesRunnable = new ProjectilesRunnable(getPlugin());

        // Set millis between recoil rotations
        Recoil.MILLIS_BETWEEN_ROTATIONS = basicConfiguration.getInt("Recoil_Millis_Between_Rotations", 5);

        registerCommands();
        registerUpdateChecker();

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Add PlayerWrapper in onEnable in case server is reloaded for example
            getPlayerWrapper(player);
        }

        long tookMillis = System.currentTimeMillis() - millisCurrent;

        // This is done like this to allow other plugins to add their own serializers
        // before WeaponMechanics starts filling those configuration mappings.
        new BukkitRunnable() {
            @Override
            public void run() {
                long millisCurrent = System.currentTimeMillis();

                loadConfig();
                registerPlaceholders();
                registerListeners();

                // Start here to ensure config values have been filled
                handleBStats();

                double seconds = NumberUtil.getAsRounded(((System.currentTimeMillis() - millisCurrent) + tookMillis) * 0.001, 2);
                debug.info("Enabled WeaponMechanics in " + seconds + "s");

            }
        }.runTask(getPlugin());

        WeaponMechanicsAPI.setInstance(this);
        debug.start(getPlugin());
    }

    void setupDebugger() {
        Logger logger = getLogger();
        int level = getConfig().getInt("Debug_Level", 2);
        boolean isPrintTraces = getConfig().getBoolean("Print_Traces", false);
        debug = new Debugger(logger, level, isPrintTraces);
        MechanicsCore.debug.setLevel(level);
        debug.permission = "weaponmechanics.errorlog";
        debug.msg = "WeaponMechanics had %s error(s) in console.";
    }

    void writeFiles() {

        // Create files
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0) {
            debug.info("Copying files from jar (This process may take up to 30 seconds during the first load!)");
            try {
                FileUtil.copyResourcesTo(getClassLoader().getResource("WeaponMechanics"), getDataFolder().toPath());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }

        try {
            // TODO bad programmars comment out broken code
            //FileUtil.ensureDefaults(getClassLoader(), "WeaponMechanics/config.yml", new File(getDataFolder(), "config.yml"));
        } catch (YAMLException e) {
            debug.error("WeaponMechanics jar corruption... This is most likely caused by using /reload after building jar!");
        }

        // Fill config.yml mappings
        File configyml = new File(getDataFolder(), "config.yml");
        if (configyml.exists()) {
            List<IValidator> validators = new ArrayList<>();
            validators.add(new HitBox()); // No need for other validators here as this is only for config.yml

            FileReader basicConfigurationReader = new FileReader(debug, null, validators);
            Configuration filledMap = basicConfigurationReader.fillOneFile(configyml);
            basicConfiguration = basicConfigurationReader.usePathToSerializersAndValidators(filledMap);
        } else {
            // Just creates empty map to prevent other issues
            basicConfiguration = new LinkedConfig();
            debug.log(LogLevel.WARN,
                    "Could not locate config.yml?",
                    "Make sure it exists in path " + getDataFolder() + "/config.yml");
        }

        // Ensure that the resource pack exists in the folder
        if (basicConfiguration.getBool("Resource_Pack_Download.Enabled")) {
            String link = basicConfiguration.getString("Resource_Pack_Download.Link");
            int connection = basicConfiguration.getInt("Resource_Pack_Download.Connection_Timeout");
            int read = basicConfiguration.getInt("Resource_Pack_Download.Read_Timeout");

            File pack = new File(getDataFolder(), "WeaponMechanicsResourcePack.zip");
            if (!pack.exists()) {
                FileUtil.downloadFile(pack, link, connection, read);
            }
        }
    }

    void loadConfig() {
        debug.debug("Loading and serializing config");

        try {
            List<?> serializers = new SerializerInstancer(new JarFile(getFile())).createAllInstances(getClassLoader());
            //noinspection unchecked
            MechanicsCore.addSerializers(getPlugin(), (List<Serializer<?>>) serializers);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (configurations == null) {
            configurations = new LinkedConfig();
        } else {
            configurations.clear();
        }

        List<IValidator> validators = null;
        try {
            // Find all validators in WeaponMechanics
            validators = new JarInstancer(new JarFile(getFile())).createAllInstances(IValidator.class, getClassLoader(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fill configuration mappings (except config.yml)
        Configuration temp = new FileReader(debug, MechanicsCore.getListOfSerializers(), validators).fillAllFiles(getDataFolder(), "config.yml");
        try {
            configurations.add(temp);
        } catch (DuplicateKeyException e) {
            debug.error("Error loading config: " + e.getMessage());
        }
    }

    void registerPlaceholders() {
        debug.debug("Registering placeholders");
        try {
            new JarInstancer(new JarFile(getFile())).createAllInstances(PlaceholderHandler.class, getClassLoader(), true).forEach(PlaceholderAPI::addPlaceholderHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void registerListeners() {
        // Register events
        // Registering events after serialization is completed to prevent any errors from happening
        debug.debug("Registering listeners");

        // TRIGGER EVENTS
        Bukkit.getPluginManager().registerEvents(new TriggerPlayerListeners(weaponHandler), getPlugin());
        Bukkit.getPluginManager().registerEvents(new TriggerEntityListeners(weaponHandler), getPlugin());

        // WEAPON EVENTS
        Bukkit.getPluginManager().registerEvents(new WeaponListeners(weaponHandler), getPlugin());
        Bukkit.getPluginManager().registerEvents(new ExplosionInteractionListeners(), getPlugin());

        // Other
        Bukkit.getPluginManager().registerEvents(new ResourcePackListener(), getPlugin());
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            Bukkit.getPluginManager().registerEvents(new MythicMobsLoader(), getPlugin());
        }
    }

    void registerPacketListeners() {
        debug.debug("Creating packet listeners");
        packetListener = new PacketHandlerListener(getPlugin(), debug);
        packetListener.addPacketHandler(new OutAbilitiesListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutEntityEffectListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutRemoveEntityEffectListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutSetSlotBobFix(getPlugin()), true);
    }

    void registerCommands() {
        debug.debug("Registering commands");
        Method getCommandMap = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftServer"), "getCommandMap");
        SimpleCommandMap commands = (SimpleCommandMap) ReflectionUtil.invokeMethod(getCommandMap, Bukkit.getServer());

        // This can occur onReload, or if another plugin registered the
        // command. We use the try-catch to determine if the command was
        // registered by another plugin.
        Command registered = commands.getCommand("weaponmechanics");
        if (registered != null) {
            try {
                mainCommand = (MainCommand) registered;
            } catch (ClassCastException ex) {
                debug.error("/weaponmechanics command was already registered... does another plugin use /wm?",
                        "The registered command: " + registered,
                        "Do not ignore this error! The weapon mechanics commands will not work at all!");
            }
        } else {
            commands.register("weaponmechanics", mainCommand = new WeaponMechanicsMainCommand());
        }

    }

    void registerUpdateChecker() {
        if (basicConfiguration.getBool("Update_Checker.Enable")) {

            debug.debug("Checking for updates");
            try {
                //Integer.parseInt("%%__RESOURCE__%%");

                int majorsBehind = basicConfiguration.getInt("Update_Checker.Required_Versions_Behind.Major", 1);
                int minorsBehind = basicConfiguration.getInt("Update_Checker.Required_Versions_Behind.Minor", 3);
                int patchesBehind = basicConfiguration.getInt("Update_Checker.Required_Versions_Behind.Patch", 1);
                SpigotResource spigotResource = new SpigotResource(getPlugin(), "99913");
                updateChecker = new UpdateChecker(spigotResource, majorsBehind, minorsBehind, patchesBehind);
            } catch (NumberFormatException e) {
                // %%__RESOURCE__%% is converted to resource ID on download (only in premium resources)
                // Here is just extra check that its been converted
                // -> If its not converted its localhost test version most likely
            }
        }
    }

    void handleBStats() {

        // See https://bstats.org/plugin/bukkit/WeaponMechanics/14323. This is
        // the bStats plugin id used to track information.
        int id = 14323;

        Metrics metrics = new Metrics((JavaPlugin) getPlugin(), id);

        // Tracks the number of weapons that are used in the plugin. Since each
        // server uses a relatively random number of weapons, we should track
        // ranges of weapons (As in, <10, >10 & <20, >20 & <30, etc). This way,
        // the pie chart will look tolerable.
        // https://bstats.org/help/custom-charts
        metrics.addCustomChart(new SimplePie("registered_weapons", () -> {
            int weapons = getWeaponHandler().getInfoHandler().getSortedWeaponList().size();

            if (weapons <= 10) {
                return "0-10";
            } else if (weapons <= 20) {
                return "11-20";
            } else if (weapons <= 30) {
                return "21-30";
            } else if (weapons <= 50) {
                return "31-50";
            } else if (weapons <= 100) {
                return "51-100";
            } else {
                return ">100";
            }
        }));

        metrics.addCustomChart(new SimplePie("custom_weapons", () -> {
            Set<String> defaultWeapons = new HashSet<>(Arrays.asList("AK-47", "FN_FAL", "FR_5_56", "M4A1",
                    "Stim",
                    "Airstrike", "Cluster_Grenade", "Flashbang", "Grenade", "Semtex",
                    "MG34",
                    "Kar98k",
                    "Combat_Knife",
                    "50_GS", "357_Magnum",
                    "RPG-7",
                    "Origin_12", "R9-0",
                    "AX-50",
                    "AUG", "Uzi"));

            InfoHandler infoHandler = getWeaponHandler().getInfoHandler();
            int counter = 0;

            for (String weapon : infoHandler.getSortedWeaponList()) {
                if (!defaultWeapons.contains(weapon)) {
                    ++counter;
                }
            }

            if (counter <= 0) {
                return "0";
            } else if (counter <= 5) {
                return "1-5";
            } else if (counter <= 10) {
                return "6-10";
            } else if (counter <= 20) {
                return "11-20";
            } else if (counter <= 30) {
                return "21-30";
            } else if (counter <= 50) {
                return "31-50";
            } else if (counter <= 100) {
                return "51-100";
            } else {
                return ">100";
            }
        }));

        metrics.addCustomChart(new SimplePie("core_version", () -> MechanicsCore.getPlugin().getDescription().getVersion()));
    }

    public TaskChain onReload() {
        MechanicsCore mechanicsCore = MechanicsCore.getPlugin();

        this.onDisable();
        mechanicsCore.onDisable();

        mechanicsCore.onLoad();
        mechanicsCore.onEnable();

        // Setup the debugger
        plugin = this;
        setupDebugger();
        entityWrappers = new HashMap<>();
        weaponHandler = new WeaponHandler();
        projectilesRunnable = new ProjectilesRunnable(getPlugin());

        return new TaskChain(getPlugin())
                .thenRunAsync(this::writeFiles)
                .thenRunSync(() -> {
                    loadConfig();
                    registerPlaceholders();
                    registerPacketListeners();
                    registerListeners();
                    registerCommands();
                    registerUpdateChecker();

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        // Add PlayerWrapper in onEnable in case server is reloaded for example
                        getPlayerWrapper(player);
                    }
                    WeaponMechanicsAPI.setInstance(this);
                });
    }

    public void onDisable() {
        BlockDamageData.regenerateAll();

        HandlerList.unregisterAll(getPlugin());
        Bukkit.getServer().getScheduler().cancelTasks(getPlugin());

        weaponHandler = null;
        updateChecker = null;
        entityWrappers = null;
        mainCommand = null;
        configurations = null;
        basicConfiguration = null;
        projectilesRunnable = null;
        plugin = null;
        debug = null;
        packetListener.close();
        packetListener = null;
        WeaponMechanicsAPI.setInstance(null);
    }

    /**
     * @return The BukkitRunnable holding the projectiles being ticked
     */
    public static ProjectilesRunnable getProjectilesRunnable() {
        return plugin.projectilesRunnable;
    }

    /**
     * @return the WeaponMechanics plugin instance
     */
    public static Plugin getPlugin() {
        return plugin.javaPlugin;
    }

    /**
     * This method can't return null because new EntityWrapper is created if not found.
     *
     * @param entity the entity wrapper to get
     * @return the entity wrapper
     */
    public static EntityWrapper getEntityWrapper(LivingEntity entity) {
        if (entity.getType() == EntityType.PLAYER) {
            return getPlayerWrapper((Player) entity);
        }
        return getEntityWrapper(entity, false);
    }

    /**
     * This method will return null if no auto add is set to true and EntityWrapper is not found.
     * If no auto add is false then new EntityWrapper is automatically created if not found and returned by this method.
     *
     * @param entity the entity
     * @param noAutoAdd true means that EntityWrapper wont be automatically added if not found
     * @return the entity wrapper or null if no auto add is true and EntityWrapper was not found
     */
    @Nullable
    public static EntityWrapper getEntityWrapper(LivingEntity entity, boolean noAutoAdd) {
        EntityWrapper wrapper = plugin.entityWrappers.get(entity);
        if (wrapper == null) {
            if (noAutoAdd) {
                return null;
            }
            wrapper = new EntityWrapper(entity);
            plugin.entityWrappers.put(entity, wrapper);
        }
        return wrapper;
    }

    /**
     * This method can't return null because new PlayerWrapper is created if not found.
     * Use mainly getEntityWrapper() instead of this unless you especially need something from PlayerWrapper.
     *
     * @param player the player wrapper to get
     * @return the player wrapper
     */
    public static PlayerWrapper getPlayerWrapper(Player player) {
        EntityWrapper wrapper = plugin.entityWrappers.get(player);
        if (wrapper == null) {
            wrapper = new PlayerWrapper(player);
            plugin.entityWrappers.put(player, wrapper);
        }
        if (!(wrapper instanceof PlayerWrapper)) {
            // Exception is better in this case as we need to know where this mistake happened
            throw new IllegalArgumentException("Tried to get PlayerWrapper from player which didn't have PlayerWrapper (only EntityWrapper)...?");
        }
        return (PlayerWrapper) wrapper;
    }

    /**
     * Removes entity (and player) wrapper and all of its content.
     * Move task is also cancelled.
     *
     * @param entity the entity (or player)
     */
    public static void removeEntityWrapper(LivingEntity entity) {
        EntityWrapper oldWrapper = plugin.entityWrappers.remove(entity);
        if (oldWrapper != null) {
            int oldMoveTask = oldWrapper.getMoveTaskId();
            if (oldMoveTask != 0) {
                Bukkit.getScheduler().cancelTask(oldMoveTask);
            }
            oldWrapper.getMainHandData().cancelTasks();
            oldWrapper.getOffHandData().cancelTasks();
        }
    }

    /**
     * This method returns ALL configurations EXCEPT config.yml used by WeaponMechanics.
     *
     * @return the configurations interface
     */
    public static Configuration getConfigurations() {
        return plugin.configurations;
    }

    /**
     * This method returns ONLY config.yml configurations used by WeaponMechanics.
     *
     * @return the configurations interface
     */
    public static Configuration getBasicConfigurations() {
        return plugin.basicConfiguration;
    }

    /**
     * @return the main command instance of WeaponMechanics
     */
    public static MainCommand getMainCommand() {
        return plugin.mainCommand;
    }

    /**
     * Returns WeaponMechanics's update checker
     *
     * @return the update checker or null if not used
     */
    @Nullable
    public static UpdateChecker getUpdateChecker() {
        return plugin.updateChecker;
    }

    /**
     * @return the current weapon handler
     */
    public static WeaponHandler getWeaponHandler() {
        return plugin.weaponHandler;
    }

    public static MCTiming timing(String name) {
        return plugin.timingManager.of(name);
    }
}