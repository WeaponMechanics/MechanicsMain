package me.deecaad.core;

import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.file.*;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.jar.JarFile;

public abstract class MechanicsPlugin extends JavaPlugin {

    private static SimpleCommandMap commands;

    protected Configuration configuration;
    protected Debugger debug;
    protected MainCommand mainCommand;

    static {
        Method getCommandMap = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftServer"), "getCommandMap");
        commands = (SimpleCommandMap) ReflectionUtil.invokeMethod(getCommandMap, Bukkit.getServer());
    }

    protected MechanicsPlugin() {
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Debugger getDebug() {
        return debug;
    }

    public void setDebug(Debugger debug) {
        this.debug = debug;
    }

    public MainCommand getMainCommand() {
        return mainCommand;
    }

    public void setMainCommand(MainCommand mainCommand) {
        this.mainCommand = mainCommand;
    }

    @Override
    public void onLoad() {
        loadConfig();

        debug = new Debugger(getLogger(), configuration.getInt("Debug_Level"), true);

        registerFlags();
    }

    @Override
    public void onEnable() {
        registerListeners();
        registerCommands();
    }

    public void onReload() {
        onDisable();

        loadConfig();
        registerListeners();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
    }

    public void loadConfig() {

        // Reset config
        if (configuration == null) {
            configuration = new LinkedConfig();
        } else {
            configuration.clear();
        }

        new FileCopier().createFromJarToDataFolder(this, getFile(), "resources", ".yml");

        try {
            List<?> serializers = new JarInstancer(new JarFile(getFile())).createAllInstances(Serializer.class, true);

            //noinspection unchecked
            FileReader reader = new FileReader((List<Serializer<?>>) serializers, null);
            configuration = reader.fillAllFiles(getDataFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerListeners() {
    }

    public void registerFlags() {
    }

    public void registerCommands() {
    }

    public static void registerCommand(Command command) {
        commands.register(command.getLabel(), command);
    }
}
