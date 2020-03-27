package me.deecaad.core.packetlistener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to hold all packet handlers of some plugin
 */
public class PacketHandlerHolder {

    private Map<String, PacketHandler> packetHandlers;

    public PacketHandlerHolder() {
        this.packetHandlers = new HashMap<>();
    }

    /**
     * @param packetHandlerName the packet handler name to find
     * @return the packet handler with given name or null if not found
     */
    public PacketHandler getPacketHandler(String packetHandlerName) {
        return this.packetHandlers.get(packetHandlerName);
    }

    /**
     * @return collection of all packet handlers from plugin
     */
    public Collection<PacketHandler> getPacketHandlers() {
        return this.packetHandlers.values();
    }

    /**
     * Adds new packet handler
     *
     * @param packetHandler the new packet handler
     */
    public void addPacketHandler(PacketHandler packetHandler) {
        this.packetHandlers.put(packetHandler.getPacketName(), packetHandler);
    }
}