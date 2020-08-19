package me.deecaad.compatibility.entity;

import me.deecaad.core.utils.BitOperation;

public interface EntityCompatibility {

    /**
     * Gets the nms entity (handle) associated with the given
     * bukkit <code>entity</code>
     *
     * @param entity Bukkit (craft) entity
     * @return NMS entity
     */
    Object getNMSEntity(org.bukkit.entity.Entity entity);

    /**
     * Gets the integer id of the given <code>entity</code>
     * @see net.minecraft.server.v1_15_R1.Entity#getId()
     *
     * @param entity bukkit entity to grab the id from
     * @return The id of the entity
     */
    int getId(org.bukkit.entity.Entity entity);

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

    Object getMetadataPacket(Object entity, BitOperation operation, boolean isAddFlags, EntityMeta...flags);

    default Object setMetadata(Object packet, boolean isAddFlags, EntityMeta...flags) {
        return setMetadata(packet, BitOperation.OR, flags);
    }

    Object setMetadata(Object packet, BitOperation operation, EntityMeta...flags);

    /**
     * Gets the <code>PacketPlayOutEntityDestory</code> used
     * to destroy <code>entity</code>
     *
     * @param entity NMS entity to remove
     * @return Packet used to remove <code>entity</code>
     */
    Object getDestroyPacket(Object entity);

    Object getGoalSelector(CustomPathfinderGoal goal);


    /**
     * This enum gives location of a bit of information inside
     * the byte from <a href="https://wiki.vg/Entity_metadata#Entity">EntityMetaData</a>.
     *
     * Note that effects here are visual, and only cause animations and/or effects.
     *
     * This is most useful when modifying <code>PacketPlayOutEntityMetadata</code> packets.
     * @see net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata
     */
    enum EntityMeta {

        FIRE(0),      // If the entity is on fire
        SNEAKING(1),  // If the entity is sneaking
        UNUSED(2),    // If the entity is mounted
        SPRINTING(3), // If the entity is running
        SWIMMING(4),  // If the entity is swimming
        INVISIBLE(5), // If the entity is invisible
        GLOWING(6),   // If the entity is glowing
        GLIDING(7);   // If the entity is gliding on an elytra

        // This represents the "index" of the bit used for this
        // data on a byte.
        private final byte flag;

        EntityMeta(int flag) {
            this.flag = (byte) flag;
        }

        public byte getFlag() {
            return this.flag;
        }

        public byte setFlag(byte data, boolean is) {
            return (byte) (is ? data | 1 << flag : data & ~(1 << flag));
        }
    }
}
