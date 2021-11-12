package me.deecaad.core.packetlistener;

import me.deecaad.core.utils.ReflectionUtil;

import javax.annotation.Nullable;

/**
 * This abstract class outlines a block of code to run when a specific packet
 * is intercepted. Should be used along with {@link PacketHandlerListener}.
 */
public abstract class PacketHandler {

    private final Class<?> packetClass;

    /**
     * Shorthand for instantiating a handler using the name of a packet.
     *
     * @param className The name of a packet. If this argument is null, then
     *                  the block of code will be run for all intercepted
     *                  packets.
     */
    public PacketHandler(@Nullable String className) {
        this(className == null ? null : ReflectionUtil.getPacketClass(className));
    }

    /**
     * Instantiates a packet handler.
     *
     * @param packetClass The {@link Class} of the packet to handle. If this
     *                    argument is null, then the block of code will be run
     *                    for all intercepted packets.
     */
    public PacketHandler(@Nullable Class<?> packetClass) {
        this.packetClass = packetClass;
    }

    /**
     * Returns the {@link Class} of whichever packet this handler accepts.
     *
     * @return The non-null {@link Class} of the handled packet.
     */
    public Class<?> getPacketClass() {
        return packetClass;
    }

    /**
     * The code that should be run whenever a packet whose {@link Class}
     * matches {@link #getPacketClass()}.
     *
     * @param wrapper The packet wrapper containing the packet.
     */
    public abstract void onPacket(Packet wrapper);
}