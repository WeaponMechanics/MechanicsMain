package me.deecaad.core;

import me.deecaad.core.events.triggers.ArmorEquipTrigger;
import me.deecaad.core.file.JarSearcher;
import me.deecaad.core.file.JarSerializers;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.mechanics.serialization.StringSerializable;
import me.deecaad.core.packetlistener.PacketListenerAPI;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

public class MechanicsCore extends JavaPlugin {

    private static MechanicsCore plugin;

    // public so people can import a static variable
    public static Debugger debug;

    private List<Serializer<?>> defaultSerializers;
    private JarFile jarFile;
    private List<JarFile> registeredPlugins;

    private Map<String, Class<StringSerializable>> stringSerializers;

    @Override
    public void onLoad() {
        plugin = this;
        debug = new Debugger(getLogger(), 3, true);
        registeredPlugins = new ArrayList<>();
        stringSerializers = new HashMap<>();
    }

    @Override
    public void onEnable() {

        // string format serializers
        try {
            jarFile = new JarFile(getFile());
        } catch (IOException e) {
            debug.log(LogLevel.ERROR, e);
        }

        registeredPlugins.add(jarFile);
        debug.info("Searching jar files for serializers");
        debug.info("Registered jars: " + registeredPlugins);
        registeredPlugins.forEach(jar -> new JarSearcher(jar).findAllSubclasses(StringSerializable.class, true).forEach(clazz -> {

            // Load serializer arguments all at once as to not worry about it later
            StringSerializable.parseArgs(clazz);
            stringSerializers.put(StringSerializable.parseName(clazz), clazz);
        }));

        // Default yaml format serializers
        defaultSerializers = new JarSerializers().getAllSerializersInsideJar(this, getFile());

        new PacketListenerAPI(this);

        ArmorEquipTrigger armorEquipTrigger = new ArmorEquipTrigger();
        PacketListenerAPI.addPacketHandler(this, armorEquipTrigger);
    }

    @Override
    public void onDisable() {
        PlaceholderAPI.onDisable();
        PacketListenerAPI.onDisable();
        plugin = null;
    }

    public JarFile getJarFile() {
        return jarFile;
    }

    /**
     * @return the MechanicsCore plugin instance
     */
    public static MechanicsCore getPlugin() {
        return plugin;
    }

    /**
     * @return all serializers within MechanicsCore
     */
    public List<Serializer<?>> getDefaultSerializers() {
        return defaultSerializers;
    }

    public Map<String, Class<StringSerializable>> getStringSerializers() {
        return stringSerializers;
    }

    public void registerJar(JarFile jar) {
        if (registeredPlugins.contains(jar)) {
            debug.warn("Plugin tried to register jar twice: " + jar);
            return;
        }
        registeredPlugins.add(jar);
    }
}
