package me.deecaad.core.packetlistener;

import me.deecaad.core.MechanicsPlugin;
import me.deecaad.core.utils.Debugger;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class outlines a {@link PacketListener} that runs different code based
 * on which {@link PacketHandler}(s) are present for each packet.
 *
 * @see PacketHandler
 */
public class PacketHandlerListener extends PacketListener {

    private final Map<Class<?>, List<PacketHandler>> inHandlers;
    private final Map<Class<?>, List<PacketHandler>> outHandlers;

    /**
     * Shorthand implementation for when your plugin is a
     * {@link MechanicsPlugin}.
     *
     * @param plugin The non-null plugin to register this listener to.
     */
    public PacketHandlerListener(@Nonnull MechanicsPlugin plugin) {
        this(plugin, plugin.getDebug());
    }

    /**
     * Default implementation of the super class constructor.
     *
     * @see PacketListener
     *
     * @param plugin   The non-null plugin to register the packet interceptors to.
     * @param debugger The non-null debugger to use for possible issues.
     */
    public PacketHandlerListener(@Nonnull Plugin plugin, @Nonnull Debugger debugger) {
        super(plugin, debugger);

        inHandlers = new HashMap<>();
        outHandlers = new HashMap<>();
    }

    /**
     * Adds the given {@link PacketHandler} <code>handler</code> to the backing
     * handler maps. If the given {@link PacketHandler} deals with outgoing
     * packets (Packets going from the server to the client), then
     * <code>out</code> should be <code>true</code>. If the handler deals with
     * incoming packets (Packets going from the client to the server), then
     * <code>out</code> should be <code>false</code>.
     *
     * @param handler The non-null handler that determines which packets are
     *                intercepted and the code to run after they are
     *                intercepted.
     * @param out     <code>true</code> if the packet is outbound.
     */
    public void addPacketHandler(@Nonnull PacketHandler handler, boolean out) {

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
