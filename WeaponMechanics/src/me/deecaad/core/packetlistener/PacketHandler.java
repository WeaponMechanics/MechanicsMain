package me.deecaad.core.packetlistener;

public abstract class PacketHandler {

    private String packetName;

    /**
     * New PacketHandler which will listen to one specific packet
     *
     * @param packetName the packet name
     */
    public PacketHandler(String packetName) {
        this.packetName = packetName;
    }

    /**
     * New PacketHandler which will listen to every packet
     *
     * @param debugger just set this to true if you use it
     */
    public PacketHandler(boolean debugger) {
        if (!debugger) {
            throw new IllegalArgumentException("Tried to register debugger packet handler when debugging was set to false...?");
        }
        this.packetName = "PacketDebugger";
    }

    /**
     * @return the packet name
     */
    public String getPacketName() {
        return this.packetName;
    }

    /**
     * If packet handler is debugger it will run onPacket(packet) everytime some packet is sent
     *
     * @return true if this packet handler is debugger
     */
    public boolean isDebugger() {
        return this.packetName.equals("PacketDebugger");
    }

    /**
     * <pre>
     * When sending packet
     * In packet = for server from player
     * Out packet = for player from server
     * </pre>
     *
     * @param packet the packet instance
     */
    public abstract void onPacket(Packet packet);
}