package me.deecaad.compatibility.entity;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

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
     * @throws IllegalArgumentException If the given object is not an nms entity
     */
    Object getSpawnPacket(Object entity);

    /**
     * Gets a velocity packet for the given NMS
     * <code>entity</code> with the given motion
     * <code>velocity</code>.
     *
     * @param entity The NMS entity to apply the velocity to
     * @param velocity The velocity
     * @return PacketPlayOutEntityVelocity
     * @throws IllegalArgumentException If the given object is not an nms entity
     */
    Object getVelocityPacket(Object entity, Vector velocity);

    /**
     * Gets the <code>PacketPlayOutEntityMetadata</code>
     * containing the metadata from the <code>DataWatcher</code>
     * from the NMS Entity <code>entity</code>
     *
     * @param entity NMS entity to display
     * @return Packet used to display <code>entity</code>
     * @throws IllegalArgumentException If the given object is not an nms entity
     */
    Object getMetadataPacket(Object entity);

    Object getMetadataPacket(Object entity, boolean isEnableFlags, EntityMeta...flags);

    Object setMetadata(Object packet, boolean isEnableFlags, EntityMeta...flags);

    /**
     * Gets the <code>PacketPlayOutEntityDestory</code> used
     * to destroy <code>entity</code>
     *
     * @param entity NMS entity to remove
     * @return Packet used to remove <code>entity</code>
     */
    Object getDestroyPacket(Object entity);

    /**
     * Spawns an NMS <code>EntityFirework</code> using packets
     * for the given <code>players</code>. The spawned
     * <code>EntityFirework</code> will have the given
     * <code>effects</code> applied to it when it explodes.
     *
     * The <code>EntityFirework</code> "explodes" aync after
     * <code>flightTime</code> amount of ticks passes.
     *
     * @param loc The bukkit location to spawn the firework at
     * @param players All of the players that will see the firework
     * @param flightTime The time before the firework explodes
     * @param effects The effects that the firework will have
     */
    void spawnFirework(Location loc, Collection<? extends Player> players, byte flightTime, FireworkEffect...effects);

    /**
     * Gets an NMS <code>PathFinderGoal</code> that can be
     * applied to entities that uses methods from the given
     * <code>CustomPathfinderGoal</code>.
     *
     * An <code>UnsupportedOperationException</code> will be thrown
     * if the implementing class cannot make subclasses of
     * <code>PathFinderGoal</code>
     * @see EntityReflection
     *
     * @param goal The goal to use
     * @return NMS PathFinderGoal
     * @throws UnsupportedOperationException If using reflection
     */
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


        private final byte mask;

        EntityMeta(int location) {
            this.mask = (byte) (1 << location);
        }

        public byte getMask() {
            return mask;
        }

        public byte set(byte data, boolean is) {
            return (byte) (is ? data | mask : data & ~(mask));
        }
    }
}