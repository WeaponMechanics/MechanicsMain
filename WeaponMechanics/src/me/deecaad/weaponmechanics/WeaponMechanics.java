package me.deecaad.weaponmechanics;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.worldguard.IWorldGuardCompatibility;
import me.deecaad.compatibility.worldguard.WorldGuardAPI;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.DuplicateKeyException;
import me.deecaad.core.file.FileCopier;
import me.deecaad.core.file.FileReader;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.JarSerializers;
import me.deecaad.core.file.LinkedConfig;
import me.deecaad.core.packetlistener.PacketHandlerListener;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.web.SpigotResource;
import me.deecaad.weaponcompatibility.projectile.HitBox;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsMainCommand;
import me.deecaad.weaponmechanics.listeners.AmmoListeners;
import me.deecaad.weaponmechanics.listeners.ExplosionInteractionListener;
import me.deecaad.weaponmechanics.listeners.WeaponListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerEntityListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerEntityListenersAbove_1_9;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerPlayerListeners;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerPlayerListenersAbove_1_9;
import me.deecaad.weaponmechanics.packetlisteners.OutAbilitiesListener;
import me.deecaad.weaponmechanics.packetlisteners.OutEntityEffectListener;
import me.deecaad.weaponmechanics.packetlisteners.OutRemoveEntityEffectListener;
import me.deecaad.weaponmechanics.packetlisteners.OutSetSlotListener;
import me.deecaad.weaponmechanics.packetlisteners.OutUpdateAttributesListener;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectilesRunnable;
import me.deecaad.weaponmechanics.weapon.reload.ReloadHandler;
import me.deecaad.weaponmechanics.weapon.scope.ScopeHandler;
import me.deecaad.weaponmechanics.weapon.shoot.ShootHandler;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeaponMechanics extends JavaPlugin {

    private static Plugin plugin;
    private static Map<LivingEntity, IEntityWrapper> entityWrappers;
    private static Configuration configurations;
    private static Configuration basicConfiguration;
    private static MainCommand mainCommand;
    private static WeaponHandler weaponHandler;
    private static UpdateChecker updateChecker;
    private static CustomProjectilesRunnable customProjectilesRunnable;

    // public so people can import a static variable
    public static Debugger debug;

    @Override
    public void onLoad() {

        if (false) {
            Logger logger = getLogger();
            logger.log(Level.SEVERE, "----------------------------------------");
            logger.log(Level.SEVERE, "No MechanicsCore detected...");
            logger.log(Level.SEVERE, "Attempting to download MechanicsCore from github");

            File directory = new File(getDataFolder().getParentFile(), "MechanicsCore.jar");
            String link = "link to jar";

            try (
                    BufferedInputStream input =
                            new BufferedInputStream(new URL(link).openStream());
                    FileOutputStream output =
                            new FileOutputStream(directory);
            ) {

                byte[] data = new byte[1024];
                int content;

                while ((content = input.read(data, 0, 1024)) != -1) {
                    output.write(data, 0, content);
                }

            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to install MechanicsCore.jar automatically!");
                logger.log(Level.SEVERE, "You'll have to download it yourself by going to the following url:");
                logger.log(Level.SEVERE, link);
            }

            logger.log(Level.SEVERE, "----------------------------------------");
        }

        // Setup the debugger
        Logger logger = getLogger();
        int level = getConfig().getInt("Debug_Level", 2);
        debug = new Debugger(logger, level, true);
        debug.permission = "weaponmechanics.errorlog";
        debug.msg = "WeaponMechanics had %s error(s) in console. Check console for instructions on why the error occurred and how to fix it.";

        // Check Java version and warn users about untested/unsupported versions
        if (ReflectionUtil.getJavaVersion() < 8) {
            debug.error("Detected a JAVA version under java 8. This plugin will NOT work in versions under java 8.");
            debug.error("Detected JAVA version: " + ReflectionUtil.getJavaVersion());
        } else if (ReflectionUtil.getJavaVersion() > 11) {
            debug.warn("Detected a JAVA version above java 11. This plugin MAY not work in versions above java 11.");
            debug.warn("Detected JAVA version: " + ReflectionUtil.getJavaVersion());
        }

        // Register all WorldGuard flags
        IWorldGuardCompatibility guard = WorldGuardAPI.getWorldGuardCompatibility();
        if (guard.isInstalled()) {
            debug.log(LogLevel.INFO, "Detected WorldGuard, registering flags");
            guard.registerFlag("weapon-shoot", IWorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-shoot-message", IWorldGuardCompatibility.FlagType.STRING_FLAG);
            guard.registerFlag("weapon-explode", IWorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-explode-message", IWorldGuardCompatibility.FlagType.STRING_FLAG);
            guard.registerFlag("weapon-break-block", IWorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-damage", IWorldGuardCompatibility.FlagType.STATE_FLAG);
            guard.registerFlag("weapon-damage-message", IWorldGuardCompatibility.FlagType.STRING_FLAG);
        } else {
            debug.log(LogLevel.DEBUG, "No WorldGuard detected!");
        }
    }

    @Override
    public void onEnable() {
        long millisCurrent = System.currentTimeMillis();

        plugin = this;
        entityWrappers = new HashMap<>();

        // Create files
        debug.info("Loading config.yml");
        new FileCopier().createFromJarToDataFolder(this, getFile(), "resources", ".yml", ".png");

        // Fill config.yml mappings
        File configyml = new File(getDataFolder(), "config.yml");
        if (configyml != null && configyml.exists()) {
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

        // Register packet listeners
        debug.info("Creating packet listeners");
        PacketHandlerListener packetListener = new PacketHandlerListener(this, debug);
        packetListener.addPacketHandler(new OutSetSlotListener(), true); // reduce/remove weapons from going up and down
        packetListener.addPacketHandler(new OutUpdateAttributesListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutAbilitiesListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutEntityEffectListener(), true); // used with scopes
        packetListener.addPacketHandler(new OutRemoveEntityEffectListener(), true); // used with scopes

        weaponHandler = new WeaponHandler();

        // Start custom projectile runnable
        customProjectilesRunnable = new CustomProjectilesRunnable(this);

        // Set millis between recoil rotations
        Recoil.MILLIS_BETWEEN_ROTATIONS = basicConfiguration.getInt("Recoil_Millis_Between_Rotations", 5);

        // Lets just use command map for all commands.
        // This allows registering new commands from configurations during runtime
        debug.info("Registering commands");
        Method getCommandMap = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftServer"), "getCommandMap");
        SimpleCommandMap simpleCommandMap = (SimpleCommandMap) ReflectionUtil.invokeMethod(getCommandMap, Bukkit.getServer());

        // Register all commands
        simpleCommandMap.register("weaponmechanics", mainCommand = new WeaponMechanicsMainCommand());

        // Start update checker task and make the instance
        if (basicConfiguration.getBool("Update_Checker.Enable")) {

            debug.info("Checking for updates");
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

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Add PlayerWrapper in onEnable in case server is reloaded for example
            getPlayerWrapper(player);
        }

        debug.info("Serializing config");

        if (configurations == null) {
            configurations = new LinkedConfig();
        } else {
            configurations.clear();
        }

        MechanicsCore.addSerializers(this, new JarSerializers().getAllSerializersInsideJar(this, getFile()));

        // This is done like this to allow other plugins to add their own serializers
        // before WeaponMechanics starts filling those configuration mappings.
        new BukkitRunnable() {
            @Override
            public void run() {

                List<IValidator> validators = new ArrayList<>();
                validators.add(new HitBox());
                validators.add(new ReloadHandler());
                validators.add(new ScopeHandler());
                validators.add(new ShootHandler());
                validators.add(new DamageHandler());

                // Fill configuration mappings (except config.yml)
                Configuration temp = new FileReader(MechanicsCore.getListOfSerializers(WeaponMechanics.this), validators).fillAllFiles(getDataFolder(), "config.yml");
                try {
                    configurations.add(temp);
                } catch (DuplicateKeyException e) {
                    debug.error("Error loading config: " + e.getMessage());
                }

                // Register events
                // Registering events after serialization is completed to prevent any errors from happening

                // TRIGGER EVENTS
                Bukkit.getServer().getPluginManager().registerEvents(new TriggerPlayerListeners(weaponHandler), WeaponMechanics.this);
                Bukkit.getServer().getPluginManager().registerEvents(new TriggerEntityListeners(weaponHandler), WeaponMechanics.this);
                if (CompatibilityAPI.getVersion() >= 1.09) {
                    Bukkit.getServer().getPluginManager().registerEvents(new TriggerPlayerListenersAbove_1_9(weaponHandler), WeaponMechanics.this);
                    Bukkit.getServer().getPluginManager().registerEvents(new TriggerEntityListenersAbove_1_9(weaponHandler), WeaponMechanics.this);
                }

                // WEAPON EVENTS
                Bukkit.getServer().getPluginManager().registerEvents(new WeaponListeners(weaponHandler), WeaponMechanics.this);

                // AMMO EVENTS
                Bukkit.getPluginManager().registerEvents(new AmmoListeners(), WeaponMechanics.this);

                // EXPLOSION EVENT
                Bukkit.getPluginManager().registerEvents(new ExplosionInteractionListener(), WeaponMechanics.this);

            }
        }.runTask(this);

        debug.info("Loading API");
        new WeaponMechanicsAPI(this);

        debug.start(this);

        long tookMillis = System.currentTimeMillis() - millisCurrent;
        double seconds = NumberUtil.getAsRounded(tookMillis * 0.001, 2);
        debug.log(LogLevel.INFO, "Enabled WeaponMechanics in " + seconds + "s");
    }

    public void onReload() {
        // todo, DON'T FILL YET
    }

    @Override
    public void onDisable() {
        BlockDamageData.regenerateAll();

        weaponHandler = null;
        updateChecker = null;
        entityWrappers = null;
        mainCommand = null;
        configurations = null;
        basicConfiguration = null;
        plugin = null;
        debug = null;
    }

    /**
     * @return The BukkitRunnable holding the projectiles being ticked
     */
    public static CustomProjectilesRunnable getCustomProjectilesRunnable() {
        return customProjectilesRunnable;
    }

    /**
     * @return the main command instance of WeaponMechanics
     */
    public static MainCommand getMainCommand() {
        return mainCommand;
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
        IEntityWrapper wrapper = entityWrappers.get(entity);
        if (wrapper == null) {
            if (noAutoAdd) {
                return null;
            }
            wrapper = new EntityWrapper(entity);
            entityWrappers.put(entity, wrapper);
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
        IEntityWrapper wrapper = entityWrappers.get(player);
        if (wrapper == null) {
            wrapper = new PlayerWrapper(player);
            entityWrappers.put(player, wrapper);
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
        IEntityWrapper oldWrapper = entityWrappers.remove(entity);
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
        return configurations;
    }

    /**
     * This method returns ONLY config.yml configurations used by WeaponMechanics.
     *
     * @return the configurations interface
     */
    public static Configuration getBasicConfigurations() {
        return basicConfiguration;
    }

    /**
     * Returns WeaponMechanics's update checker
     *
     * @return the update checker or null if not used
     */
    @Nullable
    public static UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    /**
     * @return the current weapon handler
     */
    public static WeaponHandler getWeaponHandler() {
        return weaponHandler;
    }
}