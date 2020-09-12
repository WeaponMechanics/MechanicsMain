package me.deecaad.core.packetlistener;

import me.deecaad.core.MechanicsPlugin;
import me.deecaad.core.utils.Debugger;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketHandlerListener extends PacketListener {

    private Map<Class<?>, List<PacketHandler>> inHandlers;
    private Map<Class<?>, List<PacketHandler>> outHandlers;

    public PacketHandlerListener(MechanicsPlugin plugin) {
        this(plugin, plugin.getDebug());
    }

    public PacketHandlerListener(Plugin plugin, Debugger debugger) {
        super(plugin, debugger);

        inHandlers = new HashMap<>();
        outHandlers = new HashMap<>();
    }

    public void addPacketHandler(PacketHandler handler, boolean out) {

        Class<?> packetClass = handler.getPacketClass();
        List<PacketHandler> handlers;
        if (out)
            handlers = outHandlers.computeIfAbsent(packetClass, clazz -> new ArrayList<>(1));
        else
            handlers = inHandlers.computeIfAbsent(packetClass, clazz -> new ArrayList<>(1));

        handlers.add(handler);
    }

    @Override
    protected void onPacketIn(Packet wrapper) {
        handle(wrapper, inHandlers.get(wrapper.getPacket().getClass()));
        handle(wrapper, inHandlers.get(null));
    }

    @Override
    protected void onPacketOut(Packet wrapper) {
        handle(wrapper, outHandlers.get(wrapper.getPacket().getClass()));
        handle(wrapper, outHandlers.get(null));
    }

    private static void handle(Packet wrapper, List<PacketHandler> handlers) {
        if (handlers == null || handlers.isEmpty())
            return;

        for (PacketHandler handler : handlers)
            handler.onPacket(wrapper);
    }
}
