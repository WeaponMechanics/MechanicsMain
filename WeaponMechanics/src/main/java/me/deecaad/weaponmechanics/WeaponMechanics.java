package me.deecaad.weaponmechanics;

import co.aikar.timings.lib.MCTiming;
import co.aikar.timings.lib.TimingManager;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.worldguard.IWorldGuardCompatibility;
import me.deecaad.core.compatibility.worldguard.WorldGuardAPI;
import me.deecaad.core.file.*;
import me.deecaad.core.packetlistener.PacketHandlerListener;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.core.utils.*;
import me.deecaad.core.web.SpigotResource;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsMainCommand;
import me.deecaad.weaponmechanics.listeners.AmmoListeners;
import me.deecaad.weaponmechanics.listeners.ExplosionInteractionListeners;
import me.deecaad.weaponmechanics.listeners.WeaponListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerEntityListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerEntityListenersAbove_1_9;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerPlayerListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerPlayerListenersAbove_1_9;
import me.deecaad.weaponmechanics.packetlisteners.*;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectilesRunnable;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeaponMechanics extends JavaPlugin {

    private static WeaponMechanics plugin;
    Map<LivingEntity, IEntityWrapper> entityWrappers;
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

    @Override
    public void onLoad() {

        // WeaponMechanics NEEDS MechanicsCore to run, however, people have the
        // incredible ability of having 0 abilities. AKA, they cannot read
        // "UnknownDependencyException". Instead of writing out a stacktrace
        // and having some people ask for help, we should:
        //      A. Try to download/copy/install MechanicsCore for them
        //      B. Disable the plugin
        if (Bukkit.getPluginManager().getPlugin("MechanicsCore") == null) {
            // TODO  install plugin from jar? Maybe include MechanicsCore.jar
            // TODO  in the WeaponMechanics.jar and copy it over? Possibly
            // TODO  host MechanicsCore.jar online and download using URL?

            getLogger().log(Level.WARNING, "");
            boolean installed = false;

            // try to install

            if (installed) {
                getLogger().log(Level.INFO, "Installed MechanicsCore.jar successfully!");
                return;
            }

            // Debugger has not been setup yet, use logger manually
            getLogger().log(Level.SEVERE, " !!!");
            getLogger().log(Level.SEVERE, "WeaponMechanics requires MechanicsCore in order to run!");
            getLogger().log(Level.SEVERE, "You should have gotten a 'MechanicsCore.jar' file along");
            getLogger().log(Level.SEVERE, "with 'WeaponMechanics.jar' in the zip file! Make sure you");
            getLogger().log(Level.SEVERE, "put BOTH files in the plugins folder!");
            getLogger().log(Level.SEVERE, "Disabling WeaponMechanics to avoid error.");

            getPluginLoader().disablePlugin(this);
            return;
        }

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
        IWorldGuardCompatibility guard = WorldGuardAPI.getWorldGuardCompatibility();
        if (guard.isInstalled()) {
            debug.info("Detected WorldGuard, registering flags");
            guard.registerFlag("weapon-shoot", IWorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-shoot-message", IWorldGuardCompatibility.FlagType.STRING_FLAG);
            guard.registerFlag("weapon-explode", IWorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-explode-message", IWorldGuardCompatibility.FlagType.STRING_FLAG);
            guard.registerFlag("weapon-break-block", IWorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-damage", IWorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-damage-message", IWorldGuardCompatibility.FlagType.STRING_FLAG);
        } else {
            debug.debug("No WorldGuard detected!");
        }
    }

    @Override
    public void onEnable() {
        long millisCurrent = System.currentTimeMillis();

        plugin = this;
        entityWrappers = new HashMap<>();
        timingManager = TimingManager.of(this);

        writeFiles();
        registerPacketListeners();

        weaponHandler = new WeaponHandler();

        // Start custom projectile runnable
        projectilesRunnable = new ProjectilesRunnable(this);

        // Set millis between recoil rotations
        Recoil.MILLIS_BETWEEN_ROTATIONS = basicConfiguration.getInt("Recoil_Millis_Between_Rotations", 5);

        registerCommands();
        registerUpdateChecker();

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Add PlayerWrapper in onEnable in case server is reloaded for example
            getPlayerWrapper(player);
        }

        // This is done like this to allow other plugins to add their own serializers
        // before WeaponMechanics starts filling those configuration mappings.
        new BukkitRunnable() {
            @Override
            public void run() {

                loadConfig();
                registerPlaceholders();
                registerListeners();

            }
        }.runTask(this);

        WeaponMechanicsAPI.setInstance(this);
        debug.start(this);

        long tookMillis = System.currentTimeMillis() - millisCurrent;
        double seconds = NumberUtil.getAsRounded(tookMillis * 0.001, 2);
        debug.info("Enabled WeaponMechanics in " + seconds + "s");
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
            debug.info("Copying files from jar (This process may take between 10 and 30 seconds during the first load!)");
            FileUtil.copyResourcesTo(getClass(), getClassLoader(), "WeaponMechanics", getDataFolder());
        }

        try {
            FileUtil.ensureDefaults(getClassLoader(), "WeaponMechanics/config.yml", new File(getDataFolder(), "config.yml"));
        } catch (YAMLException e) {
            debug.error("WeaponMechanics jar corruption... This is most likely caused by using /reload after building jar!");
        }

        // Ensure that the resource pack exists in the folder
        FileUtil.ensureFile(getClassLoader(), "WeaponMechanics/WeaponMechanicsResourcePack.zip",
                new File(getDataFolder(), "WeaponMechanicsResourcePack.zip"));

        // Fill config.yml mappings
        File configyml = new File(getDataFolder(), "config.yml");
        if (configyml.exists()) {
            List<IValidator> validators = new ArrayList<>();
            validators.add(new HitBox()); // No need for other validators here as this is only for config.yml

            FileReader basicConfigurationReader = new FileReader(null, validators);
            Configuration filledMap = basicConfigurationReader.fillOneFile(configyml);
            basicConfiguration = basicConfigurationReader.usePathToSerializersAndValidators(filledMap);
        } else {
            // Just creates empty map to prevent other issues
            basicConfiguration = new LinkedConfig();
            debug.log(LogLevel.WARN,
                    "Could not locate config.yml?",
                    "Make sure it exists in path " + getDataFolder() + "/config.yml");
        }
    }

    void loadConfig() {
        debug.debug("Loading and serializing config");

        try {
            List<?> serializers = new JarInstancer(new JarFile(getFile())).createAllInstances(Serializer.class, getClassLoader(), true);
            //noinspection unchecked
            MechanicsCore.addSerializers(this, (List<Serializer<?>>) serializers);
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
        Configuration temp = new FileReader(MechanicsCore.getListOfSerializers(), validators).fillAllFiles(getDataFolder(), "config.yml");
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
        Bukkit.getPluginManager().registerEvents(new TriggerPlayerListeners(weaponHandler), WeaponMechanics.this);
        Bukkit.getPluginManager().registerEvents(new TriggerEntityListeners(weaponHandler), WeaponMechanics.this);
        if (CompatibilityAPI.getVersion() >= 1.09) {
            Bukkit.getPluginManager().registerEvents(new TriggerPlayerListenersAbove_1_9(weaponHandler), WeaponMechanics.this);
            Bukkit.getPluginManager().registerEvents(new TriggerEntityListenersAbove_1_9(weaponHandler), WeaponMechanics.this);
        }

        // WEAPON EVENTS
        Bukkit.getPluginManager().registerEvents(new WeaponListeners(weaponHandler), WeaponMechanics.this);
        Bukkit.getPluginManager().registerEvents(new AmmoListeners(), WeaponMechanics.this);
        Bukkit.getPluginManager().registerEvents(new ExplosionInteractionListeners(), WeaponMechanics.this);
    }

    void registerPacketListeners() {
        debug.debug("Creating packet listeners");
        packetListener = new PacketHandlerListener(this, debug);
        packetListener.addPacketHandler(new OutUpdateAttributesListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutAbilitiesListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutEntityEffectListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutRemoveEntityEffectListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutSetSlotBobFix(this), true);
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
                Integer.parseInt("%%__RESOURCE__%%");

                int majorsBehind = basicConfiguration.getInt("Update_Checker.Required_Versions_Behind.Major", 1);
                int minorsBehind = basicConfiguration.getInt("Update_Checker.Required_Versions_Behind.Minor", 3);
                int patchesBehind = basicConfiguration.getInt("Update_Checker.Required_Versions_Behind.Patch", 1);
                SpigotResource spigotResource = new SpigotResource(this, "%%__RESOURCE__%%");
                updateChecker = new UpdateChecker(spigotResource, majorsBehind, minorsBehind, patchesBehind);
            } catch (NumberFormatException e) {
                // %%__RESOURCE__%% is converted to resource ID on download (only in premium resources)
                // Here is just extra check that its been converted
                // -> If its not converted its localhost test version most likely
            }
        }
    }

    public TaskChain onReload() {
        MechanicsCore mechanicsCore = MechanicsCore.getPlugin();

        this.onDisable();
        mechanicsCore.onDisable();

        mechanicsCore.onLoad();
        this.onLoad();

        mechanicsCore.onEnable();

        // Setup the debugger
        plugin = this;
        setupDebugger();
        entityWrappers = new HashMap<>();
        weaponHandler = new WeaponHandler();
        projectilesRunnable = new ProjectilesRunnable(this);

        return new TaskChain(this)
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

    @Override
    public void onDisable() {
        BlockDamageData.regenerateAll();

        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

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
     * @return the main command instance of WeaponMechanics
     */
    public static MainCommand getMainCommand() {
        return plugin.mainCommand;
    }

    /**
     * @return the WeaponMechanics plugin instance
     */
    public static Plugin getPlugin() {
        return plugin;
    }

    /**
     * This method can't return null because new EntityWrapper is created if not found.
     *
     * @param entity the entity wrapper to get
     * @return the entity wrapper
     */
    public static IEntityWrapper getEntityWrapper(LivingEntity entity) {
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
    public static IEntityWrapper getEntityWrapper(LivingEntity entity, boolean noAutoAdd) {
        IEntityWrapper wrapper = plugin.entityWrappers.get(entity);
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
    public static IPlayerWrapper getPlayerWrapper(Player player) {
        IEntityWrapper wrapper = plugin.entityWrappers.get(player);
        if (wrapper == null) {
            wrapper = new PlayerWrapper(player);
            plugin.entityWrappers.put(player, wrapper);
        }
        if (!(wrapper instanceof IPlayerWrapper)) {
            // Exception is better in this case as we need to know where this mistake happened
            throw new IllegalArgumentException("Tried to get PlayerWrapper from player which didn't have PlayerWrapper (only EntityWrapper)...?");
        }
        return (IPlayerWrapper) wrapper;
    }

    /**
     * Removes entity (and player) wrapper and all of its content.
     * Move task is also cancelled.
     *
     * @param entity the entity (or player)
     */
    public static void removeEntityWrapper(LivingEntity entity) {
        IEntityWrapper oldWrapper = plugin.entityWrappers.remove(entity);
        if (oldWrapper != null) {
            int oldMoveTask = oldWrapper.getMoveTask();
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