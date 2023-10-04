package me.deecaad.core.compatibility.entity;

import me.deecaad.core.compatibility.HitBox;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface EntityCompatibility {

    /**
     * Null in cases where entity is invulnerable or dead.
     *
     * @param entity the entity
     * @return the living entity's hit box or null
     */
    default HitBox getHitBox(Entity entity) {
        if (entity.isInvulnerable() || !entity.getType().isAlive() || entity.isDead()) return null;

        // This default should only be used after 1.13 R2

        HitBox hitBox = new HitBox(entity.getLocation().toVector(), getLastLocation(entity))
                .grow(getWidth(entity), getHeight(entity));
        hitBox.setLivingEntity((LivingEntity) entity);

        if (entity instanceof ComplexLivingEntity) {
            for (ComplexEntityPart entityPart : ((ComplexLivingEntity) entity).getParts()) {
                BoundingBox boxPart = entityPart.getBoundingBox();
                hitBox.addVoxelShapePart(new HitBox(boxPart.getMinX(), boxPart.getMinY(), boxPart.getMinZ(), boxPart.getMaxX(), boxPart.getMaxY(), boxPart.getMaxZ()));
            }
        }

        return hitBox;
    }

    /**
     * @param entity the entity whose last location to get
     * @return the vector of entity's last location
     */
    Vector getLastLocation(Entity entity);

    /**
     * Used to get width of entity
     *
     * @param entity the entity whose width to get
     * @return the width of entity
     */
    default double getWidth(Entity entity) {
        // 1.12 ->
        // -> entity.getWidth
        // <- 1.11
        // -> nmsEntity.width
        return entity.getWidth();
    }

    /**
     * Used to get height of entity
     *
     * @param entity the entity whose height to get
     * @return the height of entity
     */
    default double getHeight(Entity entity) {
        // 1.12 ->
        // -> entity.getHeight
        // <- 1.11
        // -> nmsEntity.height
        return entity.getHeight();
    }

    /**
     * Returns <code>true</code> if the player has that material on ender
     * pearl cool-down.
     *
     * @param player   The non-null player to check.
     * @param material The non-null material to check.
     * @return true if the material is on cool-down.
     */
    default boolean hasCooldown(Player player, Material material) {
        return player.hasCooldown(material);
    }

    /**
     * Sets the material's cooldown in ticks.
     *
     * @param player   The non-null player to have the cool-down.
     * @param material The non-null material to be on cool-down.
     * @param ticks    The time, in ticks, to show the effect.
     */
    default void setCooldown(Player player, Material material, int ticks) {
        player.setCooldown(material, ticks);
    }

    /**
     * Returns the amount of absorption hearts that the entity currently has.
     *
     * @param entity The non-null bukkit entity who has absorption hearts.
     * @return The amount of absorption hearts.
     */
    default double getAbsorption(@NotNull LivingEntity entity) {
        return entity.getAbsorptionAmount();
    }

    /**
     * Sets the amount of absorption hearts for a given <code>entity</code>.
     *
     * @param entity     The non-null bukkit entity to set the hearts of.
     * @param absorption The amount of absorption hearts.
     */
    default void setAbsorption(@NotNull LivingEntity entity, double absorption) {
        entity.setAbsorptionAmount(absorption);
    }

    /**
     * Generates an NMS non-null-list (used by the player's inventory). This is
     * used internally for the {@link me.deecaad.core.events.EntityEquipmentEvent},
     * and should probably not be used by anything else.
     *
     * @param size     The size of the list.
     * @param consumer The action to execute every item add.
     * @return The fixed size list.
     */
    List<Object> generateNonNullList(int size, TriIntConsumer<ItemStack, ItemStack> consumer);

    /**
     * Generates a {@link FakeEntity} with the given entity type as a disguise.
     *
     * @param location The non-null starting location of the entity.
     * @param type     The non-null type of the entity.
     * @param data     The nullable extra data for item/fallingblock/armorstand.
     * @return The fake entity.
     */
    FakeEntity generateFakeEntity(Location location, EntityType type, Object data);

    /**
     * Shorthand for {@link #generateFakeEntity(Location, EntityType, Object)}.
     * Generates a {@link org.bukkit.entity.Item}.
     *
     * @param location The non-null starting location of the entity.
     * @param item     The non-null item to show.
     * @return The fake entity.
     */
    default FakeEntity generateFakeEntity(Location location, ItemStack item) {
        return generateFakeEntity(location, EntityType.DROPPED_ITEM, item);
    }

    /**
     * Shorthand for {@link #generateFakeEntity(Location, EntityType, Object)}.
     * Generates a {@link org.bukkit.entity.FallingBlock}.
     *
     * @param location The non-null starting location of the entity.
     * @param block    The non-null block state to show.
     * @return The fake entity.
     */
    default FakeEntity generateFakeEntity(Location location, BlockState block) {
        return generateFakeEntity(location, EntityType.FALLING_BLOCK, block);
    }

    /**
     * Gets the id of the entity from the metadata packet. This can be used to
     * get the bukkit {@link Entity} using
     * {@link me.deecaad.core.compatibility.ICompatibility#getEntityById(World, int)}.
     *
     * @param obj The entity metadata packet.
     * @return The entity's id.
     */
    int getId(Object obj);

    /**
     * Uses a packet to spawn a fake item in the player's inventory. If the
     * player is in {@link org.bukkit.GameMode#CREATIVE}, then they will be
     * able to grab the item, regardless. This issue isn't present with
     * standard players.
     *
     * @param player The non-null player to see the change
     * @param slot   The slot number to set.
     * @param item   Which item to replace, or null.
     */
    void setSlot(Player player, EquipmentSlot slot, @Nullable ItemStack item);

    /**
     * Creates a metadata packet for the entity, force updating all metadata.
     * This can be modified by
     * {@link #modifyMetaPacket(Object, EntityMeta, boolean)} to make the
     * entity invisible, glow, etc.
     *
     * @param entity The non-null entity.
     * @return The non-null packet.
     */
    Object generateMetaPacket(Entity entity);

    /**
     * Sets the given entity metadata flag to true/false.
     *
     * @param obj     The metadata packet from {@link #generateMetaPacket(Entity)}/
     * @param meta    The meta flag you want to change.
     * @param enabled true/false.
     */
    void modifyMetaPacket(Object obj, EntityMeta meta, boolean enabled);

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
