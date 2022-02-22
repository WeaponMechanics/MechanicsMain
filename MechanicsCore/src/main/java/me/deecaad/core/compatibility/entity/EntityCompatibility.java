package me.deecaad.core.compatibility.entity;

import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface EntityCompatibility {

    default void setCooldown(Player player, Material material, int ticks) {
        player.setCooldown(material, ticks);
    }

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

    FakeEntity generateFakeEntity(Location location, EntityType type, Object data);

    default FakeEntity generateFakeEntity(Location location, ItemStack item) {
        return generateFakeEntity(location, EntityType.DROPPED_ITEM, item);
    }

    default FakeEntity generateFakeEntity(Location location, BlockState block) {
        return generateFakeEntity(location, EntityType.FALLING_BLOCK, block);
    }

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
