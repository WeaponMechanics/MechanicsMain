package me.deecaad.core.compatibility.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static me.deecaad.core.utils.NumberUtil.square;

/**
 * Defines a packet based {@link org.bukkit.entity.Entity} with no server
 * functions. Faked entities are not ticked, rendered, moved, or in any other
 * way "handled" by the server.
 *
 * <p>Fake entities are usually used for visual effects, since faked entities
 * can control appearances per player. After changing a visual effect (metadata
 * + display name + gravity + etc), a metadata packet must be sent using
 * {@link #updateMeta()}.
 */
public abstract class FakeEntity {

    protected final EntityType type;
    protected Location location;
    protected int cache = -1;

    protected final EntityMeta meta;
    protected String display;
    protected boolean gravity;

    public FakeEntity(@Nonnull Location location, @Nonnull EntityType type) {
        this.type = type;
        this.meta = new EntityMeta();
        this.location = location.clone();
        this.gravity = true;
    }

    /**
     * Returns a reference to the entity metadata mutator that effects this
     * entity. In order for changes to seen by the client, you must call
     * {@link #updateMeta()}.
     *
     * @return The non-null entity metadata mutators.
     */
    @Nonnull
    public EntityMeta getMeta() {
        return meta;
    }

    /**
     * Sets the display name of the entity. Use <code>null</code> to remove the
     * any previous display name. Supports color codes using
     * {@link org.bukkit.ChatColor}. After calling this method, use
     * {@link #updateMeta()} to show the information to clients.
     *
     * @param display The nullable entity display-name.
     */
    public void setDisplay(@Nullable String display) {
        this.display = display;
    }

    /**
     * Disables entity gravity. This has no effect server-side, and will not
     * affect motion/position/rotation/anything. Instead, this method tells the
     * client that the entity should not automatically have gravity applied.
     * After calling this method use {@link #updateMeta()} to show the
     * information to clients.
     *
     * @param gravity true -> gravity, false -> no gravity.
     */
    public void setGravity(boolean gravity) {
        this.gravity = gravity;
    }

    // * ------------------------- * //
    // *  Current Location Methods * //
    // * ------------------------- * //

    public double getX() {
        return location.getX();
    }

    public double getY() {
        return location.getY();
    }

    public double getZ() {
        return location.getZ();
    }

    public float getYaw() {
        return location.getYaw();
    }

    public float getPitch() {
        return location.getPitch();
    }

    protected void setLocation(double x, double y, double z, float yaw, float pitch) {
        location.setX(x);
        location.setY(y);
        location.setZ(z);
        location.setYaw(yaw);
        location.setPitch(pitch);
    }

    // * ------------------------- * //
    // *       Tick Methods        * //
    // * ------------------------- * //

    /**
     * Shorthand for {@link #setMotion(double, double, double)}.
     *
     * @param motion The motion the entity is moving with.
     * @see #setMotion(double, double, double)
     */
    public final void setMotion(@Nonnull Vector motion) {
        setMotion(motion.getX(), motion.getY(), motion.getZ());
    }

    /**
     * Sends an entity velocity packet to all players who can see this entity.
     *
     * @param dx The change of position in the x-axis.
     * @param dy The change of position in the y-axis.
     * @param dz The change of position in the z-axis.
     */
    public abstract void setMotion(double dx, double dy, double dz);

    /**
     * Sends an entity rotation packet to all players who can see this entity.
     *
     * @implNote
     * Implementing classes should set <code>this.location</code> using
     * {@link Location#setYaw(float)} and {@link Location#setPitch(float)}.
     *
     * @param yaw   The absolute yaw rotation of the entity.
     * @param pitch The absolute pitch rotation of the entity.
     */
    public abstract void setRotation(float yaw, float pitch);

    /**
     * Shorthand for calling {@link #setPosition(double, double, double, float, float)}.
     *
     * @param pos   The non-null new position of the entity.
     * @param yaw   The yaw to set the entity at.
     * @param pitch The pitch to set the entity at.
     */
    public final void setPosition(Vector pos, float yaw, float pitch) {
        setPosition(pos.getX(), pos.getY(), pos.getZ(), yaw, pitch, false);
    }

    /**
     * Shorthand for calling {@link #setPosition(double, double, double, float, float, boolean)}.
     *
     * @param x The new position on the x-axis.
     * @param y The new position on the y-axis.
     * @param z The new position on the z-axis.
     */
    public final void setPosition(double x, double y, double z) {
        setPosition(x, y, z, getYaw(), getPitch(), false);
    }

    /**
     * Shorthand for calling {@link #setPosition(double, double, double, float, float, boolean)}.
     *
     * @param x     The new position on the x-axis.
     * @param y     The new position on the y-axis.
     * @param z     The new position on the z-axis.
     * @param yaw   The yaw to set the entity at.
     * @param pitch The pitch to set the entity at.
     */
    public final void setPosition(double x, double y, double z, float yaw, float pitch) {
        setPosition(x, y, z, yaw, pitch, false);
    }

    /**
     * Sets position of this entity. When the new location is within 8 blocks,
     * a move-look packet is sent (using a relative position). Otherwise, a
     * teleport packet is sent (using an absolute position).
     *
     * <p>If you do not want to change the entity's yaw/pitch, you may use
     * {@link #getYaw()} and {@link #getPitch()}.
     *
     * @param x     The new position on the x-axis.
     * @param y     The new position on the y-axis.
     * @param z     The new position on the z-axis.
     * @param yaw   The yaw to set the entity at.
     * @param pitch The pitch to set the entity at.
     * @param raw   true to always use a teleport packet.
     */
    public final void setPosition(double x, double y, double z, float yaw, float pitch, boolean raw) {
        double lengthSquared = raw ? 0.0 : square(x - location.getX()) + square(y - location.getY()) + square(z - location.getZ());

        // When the change of position >8, then we cannot use the move-look
        // packet since it is limited by the size of a short. When we cannot
        // use move-look, we use a teleport packet instead.
        if (raw || lengthSquared == 0.0 || lengthSquared > 64.0) {
            setLocation(x, y, z, yaw, pitch);
            setPositionRaw(x, y, z, yaw, pitch);
        } else {
            setPositionRotation(x - location.getX(), y - location.getY(), z - location.getZ(), yaw, pitch);
            setLocation(x, y, z, yaw, pitch);
        }
    }

    // private since nobody should use this method
    private void setPositionRotation(double dx, double dy, double dz, float yaw, float pitch) {
        setPositionRotation((short) (dx * 4096), (short) (dy * 4096), (short) (dz * 4096), convertYaw(yaw), convertPitch(pitch));
    }

    /**
     * Sends an entity move-look packet to all players who can see this entity.
     * Effectively sets the relative position of the entity.
     *
     * <p>This method is protected to prevent accidental/improper usage.
     *
     * @param dx    The change of position across the x-axis.
     * @param dy    The change of position across the y-axis.
     * @param dz    The change of position across the z-axis.
     * @param yaw   The absolute yaw rotation of the entity.
     * @param pitch The absolute pitch rotation of the entity.
     */
    protected abstract void setPositionRotation(short dx, short dy, short dz, byte yaw, byte pitch);

    /**
     * Sends an entity teleport packet to all players who can see this entity.
     * Effectively sets the absolute position of the entity.
     *
     * <p>This method is protected to prevent accidental/improper usage.
     *
     * @param x     The absolute x position of the entity.
     * @param y     The absolute y position of the entity.
     * @param z     The absolute z position of the entity.
     * @param yaw   The absolute yaw rotation of the entity.
     * @param pitch The absolute pitch rotation of the entity.
     */
    protected abstract void setPositionRaw(double x, double y, double z, float yaw, float pitch);

    protected final byte convertPitch(float degrees) {
        degrees *= 256.0f / 360.0f;
        if (!type.isAlive()) {
            return (byte) -degrees;
        }
        return (byte) degrees;
    }

    protected final byte convertYaw(float degrees) {
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
     * Shows this entity to all players within range of the entity. Effectively
     * the same as calling {@link #show(Player)} for each player. Sends an
     * Add Entity packet and an Entity Meta packet.
     */
    public abstract void show();

    /**
     * Shows the entity to the given player. Sends an add Entity packet and an
     * Entity Meta packet.
     *
     * @param player The non-null player to show the entity to.
     */
    public abstract void show(@Nonnull Player player);

    /**
     * Updates the meta for all players that currently see it. This method
     * should be used after any modifications to {@link #getMeta()}.
     */
    public abstract void updateMeta();

    /**
     * Hides the entity for all players that currently see it. Sends an
     * entity destroy packet. Players will not be able to see position or
     * rotation or meta or velocity changes after calling this method (Unless
     * they are added back using {@link #show(Player)}).
     */
    public abstract void remove();

    /**
     * Hides the entity for the given player. Sends an Entity Destroy packet.
     * The player will not be able to see position or rotation or meta or
     * velocity changes after calling this method (Unless they are added back
     * using {@link #show(Player)}).
     *
     * @param player The non-null player to hide the entity from.
     */
    public abstract void remove(@Nonnull Player player);
}
