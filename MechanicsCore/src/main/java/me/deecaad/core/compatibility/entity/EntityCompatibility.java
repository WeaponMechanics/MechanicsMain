package me.deecaad.core.compatibility.entity;

import me.deecaad.core.compatibility.ICompatibility;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * This interface outlines a version dependant api that return values based on
 * different {@link Entity} input. There should be an
 * implementing class for each minecraft protocol version.
 *
 * <p>Some methods of this class require the {@link net.minecraft.server}
 * entity. Use {@link #getNMSEntity(Entity)} for these methods.
 *
 * <p>For methods that return packets, in order for those packets to be visible
 * to players, the packets need to be sent to the players. See
 * {@link ICompatibility#sendPackets(Player, Object)}.
 */
public interface EntityCompatibility {

    /**
     * Returns the NMS entity that is wrapped by <code>entity</code>.
     *
     * @param entity The non-null bukkit entity that wraps the nms entity.
     * @return The non-null NMS entity.
     */
    @Nonnull
    Object getNMSEntity(@Nonnull Entity entity);

    /**
     * Returns the unique integer id of the given <code>entity</code>. This id
     * is unique to the entity's {@link World}. To get an {@link Entity} from
     * an id, use {@link ICompatibility#getEntityById(World, int)}.
     *
     * @param entity The non-null bukkit entity to grab the id of.
     * @return The unique id of the entity.
     */
    int getId(@Nonnull Entity entity);

    /**
     * Returns a spawn packet for the given NMS entity. The returned packet
     * will make the entity physically spawn for the client, but the entity
     * will not be visible. To make the entity become visible, a metadata
     * packet needs to be sent.
     *
     * @param entity The non-null NMS entity to spawn.
     * @return The non-null spawn packet.
     * @see #getMetadataPacket(Object)
     */
    @Nonnull
    Object getSpawnPacket(@Nonnull Object entity);

    /**
     * Returns a velocity packet for the given NMS entity. The returned packet
     * will make the entity <i>appear</i> to be moving with the given
     * <code>velocity</code>.
     *
     * @param entity   The non-null NMS entity to spawn.
     * @param velocity The non-null direction and magnitude.
     * @return The non-null velocity packet.
     */
    @Nonnull
    Object getVelocityPacket(@Nonnull Object entity, @Nonnull Vector velocity);

    /**
     * Returns a metadata packet for the given NMS entity. This is the default
     * metadata packet. Entity metadata packets are required to make an entity
     * visible.
     *
     * @param entity The non-null NMS entity involved.
     * @return The non-null entity metadata packet.
     */
    @Nonnull
    Object getMetadataPacket(@Nonnull Object entity);

    /**
     * Returns a metadata packet for the given NMS entity. This packet takes
     * flags to determine how the entity appears. If you do not need any flags,
     * you should use {@link #getMetadataPacket(Object)}.
     *
     * @param entity        The non-null NMS entity involved.
     * @param isEnableFlags If <code>true</code>, the <code>flags</code> will
     *                      be enabled. Otherwise, they will be disabled.
     * @param flags         The non-null metadata flags. While this can be an
     *                      empty array, {@link #getMetadataPacket(Object)}
     *                      should be used instead.
     * @return The non-null entity metadata packet.
     */
    @Nonnull
    Object getMetadataPacket(@Nonnull Object entity, boolean isEnableFlags, @Nonnull EntityMeta... flags);

    /**
     * Sets the metadata for an existing entity metadata packet. If you do not
     * have a packet yet, you should use
     * {@link #getMetadataPacket(Object, boolean, EntityMeta...)}.
     *
     * @param packet        The non-null entity metadata packet.
     * @param isEnableFlags If <code>true</code>, the <code>flags</code> will
     *                      be enabled. Otherwise, they will be disabled.
     * @param flags         The non-null metadata flags. While this can be an
     *                      empty array, {@link #getMetadataPacket(Object)}
     *                      should be used instead.
     * @return A reference to <code>packet</code>. This can be ignored.
     */
    Object setMetadata(@Nonnull Object packet, boolean isEnableFlags, @Nonnull EntityMeta... flags);

    /**
     * Returns a destroy packet for the given NMS entity. The returned packet
     * will make the entity disappear.
     *
     * @param entity The non-null entity involved.
     * @return The non-null destroy packet.
     */
    @Nonnull
    Object getDestroyPacket(@Nonnull Object entity);

    /**
     * Spawns an NMS firework using packets. The firework will be visible to
     * the given <code>players</code>. When the firework explodes, it will
     * display the given <code>effects</code>.
     *
     * <p>The explosion occurs asynchronously, <code>flightTime</code> ticks
     * after it is launched.
     *
     * @param plugin     The non-null plugin to hold the task.
     * @param loc        The non-null world and coordinates to spawn the
     *                   firework at.
     * @param players    The non-null list of non-null players that should see
     *                   the firework.
     * @param flightTime The non-negative amount of time, in ticks, before the
     *                   firework explodes.
     * @param effects    The non-null effects that should be displayed during
     *                   the explosion.
     */
    void spawnFirework(@Nonnull Plugin plugin, @Nonnull Location loc, @Nonnull Collection<? extends Player> players,
                       @Nonnegative byte flightTime, @Nonnull FireworkEffect... effects);

    /**
     * Returns an NMS falling block entity wrapper for the given location and
     * data. This method also calculates the number of ticks it will take for
     * the block to hit the ground.
     *
     * <p>This method is a shorthand for using
     * {@link #createFallingBlock(Location, Material, byte, Vector, int)}.
     *
     * @param loc    The non-null location that the entity will spawn at.
     * @param mat    The non-null material of the block to spawn
     * @param data   The non-negative byte data for legacy materials.
     * @param motion The velocity to use to calculate the time to hit the
     *               ground, or <code>null</code> to skip the calculations.
     * @return The non-null falling block wrapper.
     */
    @Nonnull
    default FallingBlockWrapper createFallingBlock(@Nonnull Location loc, @Nonnull Material mat,
                                                   @Nonnegative byte data, @Nullable Vector motion) {
        return createFallingBlock(loc, mat, data, motion, 400);
    }

    /**
     * Returns an NMS falling block entity wrapper for the given location and
     * data. This method also calculates the number of ticks it will take for
     * the block to hit the ground.
     *
     * @param loc      The non-null location that the entity will spawn at.
     * @param mat      The non-null material of the block to spawn.
     * @param data     The non-negative byte data for legacy materials.
     * @param motion   The velocity to use to calculate the time to hit the
     *                 ground, or <code>null</code> to skip the calculations.
     * @param maxTicks The maximum number of ticks to check for the time to hit
     *                 the ground.
     * @return The non-null falling block wrapper.
     */
    @Nonnull
    FallingBlockWrapper createFallingBlock(@Nonnull Location loc, @Nonnull Material mat,
                                           @Nonnegative byte data, @Nullable Vector motion, int maxTicks);

    /**
     * Returns an NMS falling block entity wrapper for the given location and
     * data. This method also calculates the number of ticks it will take for
     * the block to hit the ground.
     *
     * <p>This method is a shorthand for using
     * {@link #createFallingBlock(Location, BlockState, Vector, int)}.
     *
     * @param loc    The non-null location that the entity will spawn at
     * @param state  The non-null appearance of the block.
     * @param motion The velocity to use to calculate the time to hit the
     *               ground, or <code>null</code> to skip the calculations.
     * @return The non-null falling block wrapper.
     */
    @Nonnull
    default FallingBlockWrapper createFallingBlock(@Nonnull Location loc, @Nonnull BlockState state,
                                                   @Nullable Vector motion) {
        return createFallingBlock(loc, state, motion, 400);
    }

    /**
     * Returns an NMS falling block entity wrapper for the given location and
     * data. This method also calculates the number of ticks it will take for
     * the block to hit the ground.
     *
     * <p>This method is a shorthand for using.
     *
     * @param loc      The non-null location that the entity will spawn at
     * @param state    The non-null appearance of the block.
     * @param motion   The velocity to use to calculate the time to hit the
     *                 ground, or <code>null</code> to skip the calculations.
     * @param maxTicks The maximum number of ticks to check for the time to hit
     *                 the ground.
     * @return The non-null falling block wrapper.
     */
    @Nonnull
    FallingBlockWrapper createFallingBlock(@Nonnull Location loc, @Nonnull BlockState state, @Nullable Vector motion, int maxTicks);

    /**
     * Returns an NMS item entity for the given data and location.
     *
     * @param item The non-null bukkit item to become an entity.
     * @param loc  The non-null location that the entity will spawn at.
     * @return The non-null NMS item entity.
     */
    @Nonnull
    default Object toNMSItemEntity(@Nonnull ItemStack item, @Nonnull Location loc) {
        if (loc.getWorld() == null)
            throw new IllegalArgumentException("world is null");

        return toNMSItemEntity(item, loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Returns an NMS item entity for the given data and location.
     *
     * @param item  The non-null bukkit item to become an entity.
     * @param world The non-null world that the entity will exist in.
     * @param x     The X coordinate to spawn the item at.
     * @param y     The Y coordinate to spawn the item at.
     * @param z     The Z coordinate to spawn the item at.
     * @return The non-null NMS item entity.
     */
    @Nonnull
    Object toNMSItemEntity(@Nonnull ItemStack item, @Nonnull World world, double x, double y, double z);

    /**
     * Returns the amount of absorption hearts that the entity currently has.
     *
     * @param entity The non-null bukkit entity who has absorption hearts.
     * @return The amount of absorption hearts.
     */
    default double getAbsorption(@Nonnull LivingEntity entity) {
        return entity.getAbsorptionAmount();
    }

    /**
     * Sets the amount of absorption hearts for a given <code>entity</code>.
     *
     * @param entity     The non-null bukkit entity to set the hearts of.
     * @param absorption The amount of absorption hearts.
     */
    default void setAbsorption(@Nonnull LivingEntity entity, double absorption) {
        entity.setAbsorptionAmount(absorption);
    }

    List<Object> generateNonNullList(int size, TriIntConsumer<ItemStack, ItemStack> consumer);

    /**
     * This enum outlines the different flags and their byte location for
     * <a href="https://wiki.vg/Entity_metadata#Entity">EntityMetaData</a>.
     */
    enum EntityMeta {

        FIRE(0),      // If the entity is on fire
        SNEAKING(1),  // If the entity is sneaking
        UNUSED(2),    // If the entity is mounted (no longer used in recent versions)
        SPRINTING(3), // If the entity is running
        SWIMMING(4),  // If the entity is swimming
        INVISIBLE(5), // If the entity is invisible
        GLOWING(6),   // If the entity is glowing
        GLIDING(7);   // If the entity is gliding using an elytra

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
