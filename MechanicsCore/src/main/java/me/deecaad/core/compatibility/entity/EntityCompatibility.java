package me.deecaad.core.compatibility.entity;

import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface EntityCompatibility {

    /**
     * Generates an NMS non-null-list (used by the player's inventory). This is used internally for the
     * {@link me.deecaad.core.events.EntityEquipmentEvent}, and should probably not be used by anything
     * else.
     *
     * @param size The size of the list.
     * @param consumer The action to execute every item add.
     * @return The fixed size list.
     */
    List<Object> generateNonNullList(int size, TriIntConsumer<ItemStack, ItemStack> consumer);

    /**
     * Generates a {@link FakeEntity} with the given entity type as a disguise.
     *
     * @param location The non-null starting location of the entity.
     * @param type The non-null type of the entity.
     * @param data The nullable extra data for item/fallingblock/armorstand.
     * @return The fake entity.
     */
    FakeEntity generateFakeEntity(Location location, EntityType type, Object data);

    /**
     * Shorthand for {@link #generateFakeEntity(Location, EntityType, Object)}. Generates a
     * {@link org.bukkit.entity.Item}.
     *
     * @param location The non-null starting location of the entity.
     * @param item The non-null item to show.
     * @return The fake entity.
     */
    default FakeEntity generateFakeEntity(Location location, ItemStack item) {
        return generateFakeEntity(location, EntityType.ITEM, item);
    }

    /**
     * Shorthand for {@link #generateFakeEntity(Location, EntityType, Object)}. Generates a
     * {@link org.bukkit.entity.FallingBlock}.
     *
     * @param location The non-null starting location of the entity.
     * @param block The non-null block state to show.
     * @return The fake entity.
     */
    default FakeEntity generateFakeEntity(Location location, BlockState block) {
        return generateFakeEntity(location, EntityType.FALLING_BLOCK, block);
    }

    /**
     * Uses a packet to spawn a fake item in the player's inventory. If the player is in
     * {@link org.bukkit.GameMode#CREATIVE}, then they will be able to grab the item, regardless. This
     * issue isn't present with standard players.
     *
     * @param player The non-null player to see the change
     * @param slot The slot number to set.
     * @param item Which item to replace, or null.
     */
    void setSlot(Player player, EquipmentSlot slot, @Nullable ItemStack item);

    /**
     * Creates a metadata packet for the entity, force updating all metadata. This can be modified by
     * {@link #modifyMetaPacket(Object, EntityMeta, boolean)} to make the entity invisible, glow, etc.
     *
     * @param entity The non-null entity.
     * @return The non-null packet.
     */
    Object generateMetaPacket(Entity entity);

    /**
     * Sets the given entity metadata flag to true/false.
     *
     * @param obj The metadata packet from {@link #generateMetaPacket(Entity)}/
     * @param meta The meta flag you want to change.
     * @param enabled true/false.
     */
    void modifyMetaPacket(Object obj, EntityMeta meta, boolean enabled);

    /**
     * This enum outlines the different flags and their byte location for
     * <a href="https://wiki.vg/Entity_metadata#Entity">EntityMetaData</a>.
     */
    enum EntityMeta {

        FIRE(0), // If the entity is on fire
        SNEAKING(1), // If the entity is sneaking
        UNUSED(2), // If the entity is mounted (no longer used in recent versions)
        SPRINTING(3), // If the entity is running
        SWIMMING(4), // If the entity is swimming
        INVISIBLE(5), // If the entity is invisible
        GLOWING(6), // If the entity is glowing
        GLIDING(7); // If the entity is gliding using an elytra

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
