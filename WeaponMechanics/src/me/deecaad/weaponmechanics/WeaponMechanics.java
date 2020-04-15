package me.deecaad.weaponmechanics;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.projectile.HitBox;
import me.deecaad.core.CoreInitializer;
import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.file.*;
import me.deecaad.core.packetlistener.PacketListenerAPI;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.commands.WeaponMechanicsMainCommand;
import me.deecaad.weaponmechanics.listeners.*;
import me.deecaad.weaponmechanics.packetlisteners.*;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectilesRunnable;
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
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponMechanics extends JavaPlugin {

    private static Plugin plugin;
    private static Map<LivingEntity, IEntityWrapper> entityWrappers;
    private static Configuration configurations;
    private static Configuration basicConfiguration;
    private MainCommand mainCommand;
    private static UpdateChecker updateChecker;
    private static WeaponHandler weaponHandler;

    private static List<Serializer<?>> tempSerializers;

    @Override
    public void onLoad() {
        DebugUtil.logger = getLogger();
    }

    @Override
    public void onEnable() {
        long millisCurrent = System.currentTimeMillis();

        plugin = this;
        entityWrappers = new HashMap<>();

        // Create files
        new FileCopier().createFromJarToDataFolder(this, getFile(), "resources", ".yml", ".png");

        // Fill config.yml mappings
        File configyml = new File(getDataFolder(), "config.yml");
        if (configyml != null && configyml.exists()) {
            basicConfiguration = new FileReader(null).fillOneFile(configyml);

            new HitBox().validateHitBoxConfigurations(basicConfiguration);
        } else {
            // Just creates empty map to prevent other issues
            basicConfiguration = new OrderedConfig();
            DebugUtil.log(LogLevel.WARN,
                    "Could not locate config.yml?",
                    "Make sure it exists in path " + getDataFolder() + "/config.yml");
        }

        // Initializer core functions
        new CoreInitializer().init(this);

        // Register packet listeners
        PacketListenerAPI.addPacketHandler(this, new OutSetSlotListener()); // reduce/remove weapons from going up and down
        PacketListenerAPI.addPacketHandler(this, new OutUpdateAttributesListener()); // used with scopes
        PacketListenerAPI.addPacketHandler(this, new OutAbilitiesListener()); // used with scopes
        PacketListenerAPI.addPacketHandler(this, new OutEntityEffectListener()); // used with scopes
        PacketListenerAPI.addPacketHandler(this, new OutRemoveEntityEffectListener()); // used with scopes

        weaponHandler = new WeaponHandler();

        // Start custom projectile runnable
        new CustomProjectilesRunnable().init(this, basicConfiguration.getBool("Async_Tasks.Projectile_Updates"));

        // Lets just use command map for all commands.
        // This allows registering new commands from configurations during runtime
        Method getCommandMap = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftServer"), "getCommandMap");
        SimpleCommandMap simpleCommandMap = (SimpleCommandMap) ReflectionUtil.invokeMethod(getCommandMap, Bukkit.getServer());

        // Register all commands
        simpleCommandMap.register("weaponmechanics", mainCommand = new WeaponMechanicsMainCommand());

        // Start update checker task and make the instance
        if (basicConfiguration.getBool("Update_Checker.Enable")) {
            try {
                Integer.parseInt("%%__RESOURCE__%%");

                int requiredVersionsBehind = basicConfiguration.getInt("Update_Checker.Required_Versions_Behind", 1);
                updateChecker = new UpdateChecker(plugin, "%%__RESOURCE__%%", requiredVersionsBehind);
                updateChecker.startCheckForUpdatesTask(this);
            } catch (NumberFormatException e) {
                // %%__RESOURCE__%% is converted to resource ID on download
                // Here is just extra check that its been converted
                // -> If its not converted its localhost test version most likely
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Add PlayerWrapper in onEnable in case server is reloaded for example
            getPlayerWrapper(player);
        }

        tempSerializers = new JarSerializers().getAllSerializersInsideJar(WeaponMechanics.this, getFile());

        // This is done like this to allow other plugins to add their own serializers
        // before WeaponMechanics starts filling those configuration mappings.
        new BukkitRunnable() {
            @Override
            public void run() {
                // Fill configuration mappings (except config.yml)
                configurations = new FileReader(tempSerializers).fillAllFiles(getDataFolder(), "config.yml", "deserializers.yml");
                tempSerializers = null;

                // Register events
                // Registering events after serialization is completed to prevent any errors from happening
                Bukkit.getServer().getPluginManager().registerEvents(new PlayerListeners(weaponHandler), WeaponMechanics.this);
                Bukkit.getServer().getPluginManager().registerEvents(new EntityListeners(weaponHandler), WeaponMechanics.this);
                if (CompatibilityAPI.getVersion() >= 1.09) {
                    Bukkit.getServer().getPluginManager().registerEvents(new PlayerListenersAbove_1_9(weaponHandler), WeaponMechanics.this);
                    Bukkit.getServer().getPluginManager().registerEvents(new EntityListenersAbove_1_9(weaponHandler), WeaponMechanics.this);
                }
            }
        }.runTask(this);

        long tookMillis = System.currentTimeMillis() - millisCurrent;
        double seconds = NumberUtils.getAsRounded(tookMillis * 0.001, 2);
        DebugUtil.log(LogLevel.DEBUG, "Enabled WeaponMechanics in " + seconds + "s");
    }

    @Override
    public void onDisable() {
        // Remove EntityWrappers just in case something odd happens
        for (LivingEntity entity : entityWrappers.keySet()) {
            removeEntityWrapper(entity);
        }
        new CoreInitializer().onDisable();
        updateChecker = null;
        weaponHandler = null;
        entityWrappers = null;
        configurations = null;
        basicConfiguration = null;
        plugin = null;
    }
    
    public void onReload() {
        long millisCurrent = System.currentTimeMillis();

        // Create files
        new FileCopier().createFromJarToDataFolder(this, getFile(), "resources", ".yml", ".png");
    
        // Fill config.yml mappings
        File configyml = new File(getDataFolder(), "config.yml");
        if (configyml != null && configyml.exists()) {
            basicConfiguration = null;
            basicConfiguration = new FileReader(null).fillOneFile(configyml);

            new HitBox().validateHitBoxConfigurations(basicConfiguration);
        } else {
            // Just creates empty map to prevent other issues
            basicConfiguration = new OrderedConfig();
            DebugUtil.log(LogLevel.WARN,
                    "Could not locate config.yml inside?",
                    "Make sure it exists in path " + getDataFolder() + "/config.yml");
        }
        configurations = null;

        // todo: add on reload event to allow other plugins register their serializers on reload?
        configurations = new FileReader(new JarSerializers().getAllSerializersInsideJar(this, getFile())).fillAllFiles(getDataFolder(), "config.yml", "deserializers.yml");

        long tookMillis = System.currentTimeMillis() - millisCurrent;
        double seconds = NumberUtils.getAsRounded(tookMillis * 0.001, 2);
        DebugUtil.log(LogLevel.DEBUG, "Reloaded WeaponMechanics in " + seconds + "s");
    }
    
    public MainCommand getMainCommand() {
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
        if (oldWrapper != null && oldWrapper.getMoveTask() != 0) {
            Bukkit.getScheduler().cancelTask(oldWrapper.getMoveTask());
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
    public static UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    /**
     * @return the current weapon handler
     */
    public static WeaponHandler getWeaponHandler() {
        return weaponHandler;
    }

    /**
     * Sets new weapon handler for WeaponMechanics.
     * It is up to you how you use this. You can override all methods used by
     * default if you want to or simply some methods you want to modify some.
     *
     * WeaponMechanics doesn't notify you about changes in weapon handler code
     * so you will have to be careful when using this method. It is recommended
     * to use super.method() and after that add your new stuff you want to add, this
     * way the compatibility with WeaponMechanics should stay, but it is still not guaranteed compatibility!
     *
     * @param weaponHandler the new weapon handler
     */
    public static void setWeaponHandler(WeaponHandler weaponHandler) {
        if (weaponHandler == null) throw new NullPointerException("Someone tried to set null weapon handler...");
        WeaponMechanics.weaponHandler = weaponHandler;
    }

    /**
     * Registers given serializers. Make sure to use this method in onEnable or onLoad!
     *
     * @param serializers the serializers to add
     */
    public static void addSerializers(List<Serializer<?>> serializers) {
        if (configurations != null) {
            throw new IllegalArgumentException("You can't register serializers anymore, do it in onEnable");
        }
        serializers.forEach(WeaponMechanics::addSerializer);
    }

    /**
     * Registers given serializer. Make sure to use this method in onEnable or onLoad!
     *
     * @param serializer the serializer to add
     */
    public static void addSerializer(Serializer<?> serializer) {
        if (configurations != null) {
            throw new IllegalArgumentException("You can't register serializers anymore, do it in onEnable");
        }
        if (tempSerializers == null) {
            tempSerializers = new ArrayList<>();
        }
        tempSerializers.add(serializer);
    }
}