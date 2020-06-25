package me.deecaad.compatibility.entity;

public interface EntityCompatibility {

    /**
     * Gets the <code>PacketPlayOutSpawnEntity</code> used
     * to spawn <code>entity</code>
     *
     * @param entity NMS entity to spawn
     * @return Packet used to spawn <code>entity</code>
     */
    Object getSpawnPacket(Object entity);

    /**
     * Gets the <code>PacketPlayOutEntityMetadata</code>
     * used to display <code>entity</code>
     *
     * @param entity NMS entity to display
     * @return Packet used to display <code>entity</code>
     */
    Object getMetadataPacket(Object entity);

    /**
     * Gets the <code>PacketPlayOutEntityDestory</code> used
     * to destroy <code>entity</code>
     *
     * @param entity NMS entity to remove
     * @return Packet used to remove <code>entity</code>
     */
    Object getDestroyPacket(Object entity);
}
