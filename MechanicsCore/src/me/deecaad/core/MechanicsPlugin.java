package me.deecaad.core;

import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.FileCopier;
import me.deecaad.core.file.FileReader;
import me.deecaad.core.file.JarSerializers;
import me.deecaad.core.file.SeparatedConfig;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;

public abstract class MechanicsPlugin extends JavaPlugin {

    private SimpleCommandMap commands;

    protected Configuration configuration;
    protected Debugger debug;
    protected MainCommand mainCommand;

    protected MechanicsPlugin() {
        Method getCommandMap = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftServer"), "getCommandMap");
        commands = (SimpleCommandMap) ReflectionUtil.invokeMethod(getCommandMap, Bukkit.getServer());
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
    public abstract void onEnable();

    public void onReload() {
        onDisable();

        loadConfig();
        registerListeners();
    }

    @Override
    public abstract void onDisable();

    public void loadConfig() {

        // Reset config
        if (configuration == null) {
            configuration = new SeparatedConfig();
        } else {
            configuration.clear();
        }

        new FileCopier().createFromJarToDataFolder(this, getFile(), "resources", "yml");
        List<Serializer<?>> serializers = new JarSerializers().getAllSerializersInsideJar(this, getFile());
        FileReader reader = new FileReader(serializers, null);
    }

    public abstract void registerListeners();

    public void registerCommand(Command command) {
        commands.register(command.getLabel(), command);
    }
}
