package me.deecaad.core;

import me.deecaad.core.events.triggers.ArmorEquipTrigger;
import me.deecaad.core.file.JarSerializers;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.packetlistener.PacketListenerAPI;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

public class MechanicsCore extends JavaPlugin {

    private static MechanicsCore plugin;

    // public so people can import a static variable
    public static Debugger debug;

    private List<Serializer<?>> defaultSerializers;
    private JarFile jarFile;

    @Override
    public void onEnable() {

        plugin = this;
        debug = new Debugger(getLogger(), 2, true);
        defaultSerializers = new JarSerializers().getAllSerializersInsideJar(this, getFile());
        try {
            jarFile = new JarFile(getFile());
        } catch (IOException e) {
            debug.log(LogLevel.ERROR, e);
        }
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
}
