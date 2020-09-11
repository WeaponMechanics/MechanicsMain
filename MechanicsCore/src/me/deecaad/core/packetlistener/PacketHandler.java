package me.deecaad.core.packetlistener;

import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

public abstract class PacketHandler {

    private final Class<?> packetClass;

    public PacketHandler(@Nullable String className) {
        this(className == null ? null : ReflectionUtil.getNMSClass(className));
    }

    public PacketHandler(@Nullable Class<?> packetClass) {
        this.packetClass = packetClass;
    }

    public Class<?> getPacketClass() {
        return packetClass;
    }

    /**
     * @param packet the packet instance
     */
    public abstract void onPacket(Packet packet);
}