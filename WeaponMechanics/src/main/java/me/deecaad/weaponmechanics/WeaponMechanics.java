package me.deecaad.weaponmechanics;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.jeff_media.updatechecker.UserAgentBuilder;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.worldguard.WorldGuardCompatibility;
import me.deecaad.core.database.Database;
import me.deecaad.core.database.MySQL;
import me.deecaad.core.database.SQLite;
import me.deecaad.core.events.QueueSerializerEvent;
import me.deecaad.core.file.*;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.core.utils.*;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsCommand;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsMainCommand;
import me.deecaad.weaponmechanics.lib.MythicMobsLoader;
import me.deecaad.weaponmechanics.listeners.ExplosionInteractionListeners;
import me.deecaad.weaponmechanics.listeners.RepairItemListener;
import me.deecaad.weaponmechanics.listeners.ResourcePackListener;
import me.deecaad.weaponmechanics.listeners.WeaponListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerEntityListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerPlayerListeners;
import me.deecaad.weaponmechanics.packetlisteners.OutAbilitiesListener;
import me.deecaad.weaponmechanics.packetlisteners.OutEntityEffectListener;
import me.deecaad.weaponmechanics.packetlisteners.OutRemoveEntityEffectListener;
import me.deecaad.weaponmechanics.packetlisteners.OutSetSlotBobFix;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.damage.AssistData;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.damage.DamageModifier;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import me.deecaad.weaponmechanics.weapon.placeholders.PlaceholderValidator;
import me.deecaad.weaponmechanics.weapon.projectile.HitBoxValidator;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectilesRunnable;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoRegistry;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.weapon.stats.PlayerStat;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;
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
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class WeaponMechanics {

    private static WeaponMechanics plugin;
    JavaPlugin javaPlugin;
    Map<LivingEntity, EntityWrapper> entityWrappers;
    Configuration configurations;
    Configuration basicConfiguration;
    MainCommand mainCommand;
    WeaponHandler weaponHandler;
    ResourcePackListener resourcePackListener;
    ProjectilesRunnable projectilesRunnable;
    ProtocolManager protocolManager;
    Metrics metrics;
    Database database;

    // public so people can import a static variable
    public static Debugger debug;

    public WeaponMechanics(JavaPlugin javaPlugin) {
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
        return (ClassLoader) ReflectionUtil.invokeMethod(ReflectionUtil.getMethod(JavaPlugin.class, "getClassLoader"), javaPlugin);
    }

    public File getFile() {
        return (File) ReflectionUtil.invokeMethod(ReflectionUtil.getMethod(JavaPlugin.class, "getFile"), javaPlugin);
    }

    public void onLoad() {
        setupDebugger();

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

        try {
            JarSearcher searcher = new JarSearcher(new JarFile(getFile()));
            searcher.findAllSubclasses(Mechanic.class, getClassLoader(), true)
                    .stream().map(ReflectionUtil::newInstance).forEach(Mechanics.MECHANICS::add);
            searcher.findAllSubclasses(Targeter.class, getClassLoader(), true)
                    .stream().map(ReflectionUtil::newInstance).forEach(Mechanics.TARGETERS::add);
            searcher.findAllSubclasses(Condition.class, getClassLoader(), true)
                    .stream().map(ReflectionUtil::newInstance).forEach(Mechanics.CONDITIONS::add);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void onEnable() {
        long millisCurrent = System.currentTimeMillis();

        plugin = this;
        entityWrappers = new HashMap<>();

        writeFiles();
        registerPacketListeners();

        weaponHandler = new WeaponHandler();
        projectilesRunnable = new ProjectilesRunnable(getPlugin());
        resourcePackListener = new ResourcePackListener();

        // Set millis between recoil rotations
        Recoil.MILLIS_BETWEEN_ROTATIONS = basicConfiguration.getInt("Recoil_Millis_Between_Rotations", 20);

        setupDatabase();
        registerPlaceholders();

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Add PlayerWrapper in onEnable in case server is reloaded for example
            PlayerWrapper playerWrapper = getPlayerWrapper(player);
            weaponHandler.getStatsHandler().load(playerWrapper);
        }

        // Configuration is serialized the tick after the server starts. This
        // is done so addons (Like WeaponMechanicsCosmetics) can use the
        // QueueSerializersEvent to register their own serializers. As a
        // side note, not all tasks can be run after the server starts.
        // Commands, for example, can't be registered after onEnable without
        // some disgusting NMS shit.
        new TaskChain(javaPlugin)
                .thenRunSync(() -> {
                    loadConfig();
                    registerListeners();
                    registerBStats();
                    registerPermissions();
                });


        registerCommands();
        registerUpdateChecker();

        long tookMillis = System.currentTimeMillis() - millisCurrent;
        double seconds = NumberUtil.getAsRounded(tookMillis * 0.001, 2);
        debug.debug("Enabled WeaponMechanics in " + seconds + "s");

        // Shameless self-promotion
        if (Bukkit.getPluginManager().getPlugin("WeaponMechanicsCosmetics") == null)
            debug.info("Buy WeaponMechanicsCosmetics to support our development: https://www.spigotmc.org/resources/104539/");

        // Detect Vivecraft-Spigot-Extensions and suggest switching to VivecraftSpigot
        if (Bukkit.getPluginManager().getPermission("Vivecraft_Spigot_Extensions") != null) {
            debug.warn("Detected 'Vivecraft_Spigot_Extensions' on your server");
            debug.warn("For better compatibility with WeaponMechanics, we recommend switching to VivecraftSpigot");
            debug.warn("VivecraftSpigot: https://www.spigotmc.org/resources/104539/");
        }

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
        debug.debug("Writing files and filling basic configuration");

        // Create files
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0) {
            debug.info("Copying files from jar (This process may take up to 30 seconds during the first load!)");
            FileUtil.copyResourcesTo(getClassLoader().getResource("WeaponMechanics"), getDataFolder().toPath());
        }

        try {
            FileUtil.ensureDefaults(getClassLoader().getResource("WeaponMechanics/config.yml"), new File(getDataFolder(), "config.yml"));
        } catch (YAMLException e) {
            debug.error("WeaponMechanics jar corruption... This is most likely caused by using /reload after building jar!");
        }

        // Fill config.yml mappings
        File configyml = new File(getDataFolder(), "config.yml");
        if (configyml.exists()) {
            List<IValidator> validators = new ArrayList<>();
            validators.add(new HitBoxValidator());
            validators.add(new PlaceholderValidator());
            validators.add(new AssistData());

            FileReader basicConfigurationReader = new FileReader(debug, List.of(new DamageModifier()), validators);
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
            new TaskChain(WeaponMechanics.getPlugin())
                    .thenRunAsync((data) -> {
                        String link = basicConfiguration.getString("Resource_Pack_Download.Link");
                        int connection = basicConfiguration.getInt("Resource_Pack_Download.Connection_Timeout");
                        int read = basicConfiguration.getInt("Resource_Pack_Download.Read_Timeout");

                        if (("https://raw.githubusercontent.com/WeaponMechanics/MechanicsMain/master/WeaponMechanicsResourcePack.zip").equals(link)) {
                            try {
                                link = "https://raw.githubusercontent.com/WeaponMechanics/MechanicsMain/master/resourcepack/WeaponMechanicsResourcePack-" + resourcePackListener.getResourcePackVersion() + ".zip";
                            } catch (InternalError e) {
                                debug.log(LogLevel.DEBUG, "Failed to fetch resource pack version due to timeout", e);
                                return null;
                            }
                        }

                        File pack = new File(getDataFolder(), "WeaponMechanicsResourcePack.zip");
                        if (!pack.exists()) {
                            FileUtil.downloadFile(pack, link, connection, read);
                        }
                        return null;
                    });
        }
    }

    void setupDatabase() {
        if (basicConfiguration.getBool("Database.Enable", true)) {

            debug.debug("Setting up database");

            if (basicConfiguration.getString("Database.Type", "SQLITE").equals("SQLITE")) {
                String absolutePath = basicConfiguration.getString("Database.SQLite.Absolute_Path", "plugins/WeaponMechanics/weaponmechanics.db");
                try {
                    database = new SQLite(absolutePath);
                } catch (IOException | SQLException e) {
                    debug.log(LogLevel.WARN, "Failed to initialized database!", e);
                }
            } else {
                String hostname = basicConfiguration.getString("Database.MySQL.Hostname", "localhost");
                int port = basicConfiguration.getInt("Database.MySQL.Port", 3306);
                String databaseName = basicConfiguration.getString("Database.MySQL.Database", "weaponmechanics");
                String username = basicConfiguration.getString("Database.MySQL.Username", "root");
                String password = basicConfiguration.getString("Database.MySQL.Password", "");
                database = new MySQL(hostname, port, databaseName, username, password);
            }
            database.executeUpdate(true, PlayerStat.getCreateTableString(), WeaponStat.getCreateTableString());
        }
    }

    void loadConfig() {
        debug.debug("Loading and serializing config");

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
        AmmoRegistry.init();

        try {
            QueueSerializerEvent event = new QueueSerializerEvent(javaPlugin, getDataFolder());
            event.addSerializers(new SerializerInstancer(new JarFile(getFile())).createAllInstances(getClassLoader()));
            event.addValidators(validators);
            Bukkit.getPluginManager().callEvent(event);

            Configuration temp = new FileReader(debug, event.getSerializers(), event.getValidators()).fillAllFiles(getDataFolder(), "config.yml", "repair_kits", "attachments", "ammos", "placeholders");
            configurations.add(temp);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DuplicateKeyException e) {
            debug.error("Error loading config: " + e.getMessage());
        }
    }

    void registerPlaceholders() {
        debug.debug("Registering placeholders");
        try {
            new JarInstancer(new JarFile(getFile())).createAllInstances(PlaceholderHandler.class, getClassLoader(), true).forEach(PlaceholderHandler.REGISTRY::add);
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
        Bukkit.getPluginManager().registerEvents(resourcePackListener, getPlugin());
        Bukkit.getPluginManager().registerEvents(RepairItemListener.getInstance(), getPlugin());
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {

            // We need to make sure we are running MM v5
            PluginDescriptionFile desc = Bukkit.getPluginManager().getPlugin("MythicMobs").getDescription();
            if (!desc.getVersion().split("\\.")[0].contains("5")) {
                debug.warn("Could not hook into MythicMobs because it is outdated");
            } else {
                Bukkit.getPluginManager().registerEvents(new MythicMobsLoader(), getPlugin());
                debug.info("Hooked in MythicMobs " + desc.getVersion());
            }
        }
    }

    void registerPacketListeners() {
        debug.debug("Creating packet listeners");
        protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new OutAbilitiesListener(javaPlugin));
        protocolManager.addPacketListener(new OutEntityEffectListener(javaPlugin));
        protocolManager.addPacketListener(new OutRemoveEntityEffectListener(javaPlugin));
        protocolManager.addPacketListener(new OutSetSlotBobFix(javaPlugin));
    }

    void registerCommands() {
        debug.debug("Registering commands");

        // In 1.13+, we should use the built-in 'brigadier' system which
        // has really nice tab-completions/validation
        if (ReflectionUtil.getMCVersion() >= 13) {
            WeaponMechanicsCommand.build();
            return;
        }

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

    void registerPermissions() {
        debug.debug("Registering permissions");

        Permission parent = Bukkit.getPluginManager().getPermission("weaponmechanics.use.*");
        if (parent == null) {
            // Some older versions register permissions after onEnable...
            new TaskChain(javaPlugin).thenRunSync(this::registerPermissions);
            return;
        }

        for (String weaponTitle : weaponHandler.getInfoHandler().getSortedWeaponList()) {
            String permissionName = "weaponmechanics.use." + weaponTitle;
            Permission permission = Bukkit.getPluginManager().getPermission(permissionName);

            if (permission == null) {
                permission = new Permission(permissionName, "Permission to use " + weaponTitle);
                Bukkit.getPluginManager().addPermission(permission);
            }

            permission.addParent(parent, true);
        }
    }

    void registerUpdateChecker() {
        if (!basicConfiguration.getBool("Update_Checker.Enable", true)) return;

        debug.debug("Registering update checker");
        new UpdateChecker(javaPlugin, UpdateCheckSource.SPIGOT, "99913")
                .setNotifyOpsOnJoin(true)
                .setUserAgent(new UserAgentBuilder().addPluginNameAndVersion())
                .checkEveryXHours(24)
                .checkNow();
    }

    void registerBStats() {
        if (this.metrics != null) return;

        debug.debug("Registering bStats");

        // See https://bstats.org/plugin/bukkit/WeaponMechanics/14323. This is
        // the bStats plugin id used to track information.
        int id = 14323;

        this.metrics = new Metrics((JavaPlugin) getPlugin(), id);

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
            Set<String> defaultWeapons = new HashSet<>(Arrays.asList("AK_47", "FN_FAL", "FR_5_56", "M4A1",
                    "Stim",
                    "Airstrike", "Cluster_Grenade", "Flashbang", "Grenade", "Semtex",
                    "MG34",
                    "Kar98k",
                    "Combat_Knife",
                    "50_GS", "357_Magnum",
                    "RPG-7", "RPG_7",
                    "Origin_12", "R9-0", "R9_0",
                    "AX-50", "AX_50",
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
        JavaPlugin mechanicsCore = MechanicsCore.getPlugin();

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
        resourcePackListener = new ResourcePackListener();

        return new TaskChain(getPlugin())
                .thenRunAsync(this::writeFiles)
                .thenRunSync(() -> {

                    loadConfig();
                    registerPacketListeners();
                    registerListeners();
                    registerCommands();
                    registerPermissions();
                    registerUpdateChecker();
                    setupDatabase();

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        // Add PlayerWrapper in onEnable in case server is reloaded for example

                        PlayerWrapper playerWrapper = getPlayerWrapper(player);
                        weaponHandler.getStatsHandler().load(playerWrapper);
                    }
                });
    }

    public void onDisable() {
        BlockDamageData.regenerateAll();

        HandlerList.unregisterAll(getPlugin());
        Bukkit.getServer().getScheduler().cancelTasks(getPlugin());

        // Close database and save data in SYNC
        if (database != null) {
            for (EntityWrapper entityWrapper : entityWrappers.values()) {
                if (!entityWrapper.isPlayer()) continue;
                weaponHandler.getStatsHandler().save((PlayerWrapper) entityWrapper, true);
            }
            try {
                database.close();
            } catch (SQLException e) {
                debug.log(LogLevel.WARN, "Couldn't close database properly...", e);
            }
        }

        // Unregister packet listeners
        protocolManager.removePacketListeners(javaPlugin);

        database = null;
        weaponHandler = null;
        //updateChecker = null; do not reset update checker
        entityWrappers = null;
        mainCommand = null;
        configurations = null;
        basicConfiguration = null;
        projectilesRunnable = null;
        plugin = null;
        debug = null;
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

    public static WeaponMechanics getInstance() {
        return plugin;
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
     * @param entity    the entity
     * @param noAutoAdd true means that EntityWrapper wont be automatically added if not found
     * @return the entity wrapper or null if no auto add is true and EntityWrapper was not found
     */
    @Nullable
    public static EntityWrapper getEntityWrapper(LivingEntity entity, boolean noAutoAdd) {
        if (entity.getType() == EntityType.PLAYER) {
            return getPlayerWrapper((Player) entity);
        }
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
     * @return the current weapon handler
     */
    public static WeaponHandler getWeaponHandler() {
        return plugin.weaponHandler;
    }

    /**
     * @return the database instance if enabled
     */
    @Nullable
    public static Database getDatabase() {
        return plugin.database;
    }
}
