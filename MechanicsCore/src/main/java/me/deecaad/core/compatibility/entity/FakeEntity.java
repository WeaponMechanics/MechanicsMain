package me.deecaad.core.compatibility.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static me.deecaad.core.compatibility.entity.BitMutator.*;

/**
 * Defines a packet based {@link org.bukkit.entity.Entity} with no server
 * functions. Faked entities are not ticked, rendered, moved, or in any other
 * way "handled" by the server.
 *
 * <p>Fake entities are usually used for visual effects, since faked entities
 * can control appearances per player.
 *
 * <p>When using a faked entity, you must follow a strict order. First,
 * construct the entity, and set instance variables. Then, show the entity
 * using 1 of the <code>show()</code> method overloads. If you want to show
 * the entity to new players, or at a new time, you must first
 * {@link #clone()} this object before re-sending it. Note that you should
 * probably clone <i>before</i> you use a <code>show()</code> method.
 */
public abstract class FakeEntity {

    private final EntityType type;
    private final EntityMeta meta;
    protected int cache = -1;

    public FakeEntity(@Nonnull Location location, @Nonnull EntityType type, @Nullable Object data) {
        init(location, type, data);
        this.type = type;
        this.meta = new EntityMeta();
    }

    protected abstract void init(@Nonnull Location location, @Nonnull EntityType type, @Nullable Object data);

    /**
     * Returns a reference to the entity metadata mutator that effects this
     * entity. In order for changes to seen by the client, you must call
     * {@link #showMeta(Player)}.
     *
     * @return The non-null entity metadata mutators.
     */
    @Nonnull
    public EntityMeta getMeta() {
        return meta;
    }

    // * ------------------------- * //
    // *       Tick Methods        * //
    // * ------------------------- * //

    public void setMotion(@Nonnull Vector motion) {
        setMotion(motion.getX(), motion.getY(), motion.getZ());
    }

    public abstract void setMotion(double x, double y, double z);

    public void setPosition(Location position) {
        setPosition(position.getX(), position.getY(), position.getZ());
    }

    public void setPosition(Vector position) {
        setPosition(position.getX(), position.getY(), position.getZ());
    }

    public abstract void setPosition(double x, double y, double z);

    public void setRotation(float yaw, float pitch) {
        setRotation(yaw, pitch, false);
    }

    public abstract void setRotation(float yaw, float pitch, boolean absolute);

    public void setPositionRotation(double dx, double dy, double dz, float yaw, float pitch) {
        setPositionRotation((short) (dx * 4096), (short) (dy * 4096), (short) (dz * 4096), convertYaw(yaw), convertPitch(pitch));
    }

    public abstract void setPositionRotation(short dx, short dy, short dz, byte yaw, byte pitch);

    private byte convertPitch(float degrees) {
        degrees *= 256.0f / 360.0f;
        if (!type.isAlive()) {
            return (byte) -degrees;
        }
        return (byte) degrees;
    }

    private byte convertYaw(float degrees) {
        degrees *= 256.0f / 360.0f;
        switch (type) {
            case ARROW:
                return (byte) -degrees;
            case WITHER_SKULL:
            case ENDER_DRAGON:
                return (byte) (degrees - 128.0f);
            default:
                if (!type.isAlive() && type != EntityType.ARMOR_STAND) {
                    return (byte) (degrees - 64.0f);
                }
                return (byte) degrees;
        }
    }

    // * ------------------------- * //
    // *   Packet Based Methods    * //
    // * ------------------------- * //

    /**
     *
     */
    public abstract void show(Player player);

    /**
     * Should be called after any changes to the metadata returned by
     * {@link #getMeta()}. If {@link #show(Player)} has not yet been called,
     * that method should be called instead.
     *
     * @throws IllegalStateException If show() has not been called.
     */
    public abstract void showMeta(Player player);

    /**
     * Removes
     *
     * @throws IllegalStateException If show() has not been called.
     */
    public abstract void remove(Player player);
}
