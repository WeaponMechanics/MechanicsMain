package me.deecaad.weaponmechanics.weapon.projectile;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICustomProjectile {

    /**
     * @return the shooter of projectile
     */
    LivingEntity getShooter();

    @Nullable
    ItemStack getWeaponStack();

    @Nullable
    String getWeaponTitle();

    /**
     * This does not return the disguise entity id, but WeaponMechanics own unique identifier.
     * Use {@link #getProjectileDisguiseId()} to get disguise entity id if its used.
     *
     * @return the unique id of this custom projectile
     */
    int getUniqueId();

    /**
     * @return the world where this custom projectile is in
     */
    World getWorld();

    /**
     * @return the clone of last location
     */
    Vector getLastLocation();

    /**
     * @return the clone of current location
     */
    Vector getLocation();

    /**
     * This is used to set new location for projectile.
     * Remember to use {@link #updateDisguiseLocationAndMotion()} after using this.
     * You can also first use this and {@link #setMotion(Vector)} methods and then call that update.
     *
     * @param location the new location for projectile
     */
    void setLocation(@Nonnull Vector location);

    /**
     * @return the clone of current motion
     */
    Vector getMotion();

    /**
     * @return the current motion's length
     */
    double getMotionLength();

    /**
     * This is used to set new motion for projectile.
     * Remember to use {@link #updateDisguiseLocationAndMotion()} after using this.
     * You can also first use this and {@link #setLocation(Vector)} methods and then call that update.
     *
     * @param motion the new motion for projectile
     */
    void setMotion(@Nonnull Vector motion);

    /**
     * @return the distance projectile has travelled where 1.0 equals 1 block
     */
    double getDistanceTravelled();

    /**
     * Used to fetch any temporary data from projectiles
     *
     * @param key the key to fetch
     * @return the value of key or null if not found
     */
    @Nullable
    String getTag(String key);

    /**
     * This can store temporary data for projectiles
     *
     * @param key the key to use
     * @param value the value for key
     */
    void setTag(@Nonnull String key, @Nonnull String value);

    /**
     * @return true if projectile is marked for removal
     */
    boolean isDead();

    /**
     * Marks projectile for removal and will be removed on next tick
     */
    void remove();

    /**
     * @return true if this projectile has diguise
     */
    boolean hasProjectileDisguise();

    /**
     * Updates projectile disguise location and motion based on current location and motion
     * for all players within 90 blocks.
     */
    void updateDisguiseLocationAndMotion();

    /**
     * Used to get the disguise entity's id.
     * This is mainly be used for sending packets to notify player
     * about updates in projectile's movement.
     *
     * @return the disguise projectile id or 0 if disguise is not used
     */
    int getProjectileDisguiseId();

    /**
     * @return the yaw of projectile or 0 if disguise is not used
     */
    float getProjectileDisguiseYaw();

    /**
     * @return the pitch of projectile or 0 if disguise is not used
     */
    float getProjectileDisguisePitch();

    /**
     * @return the projectile settings for this projectile
     */
    Projectile getProjectileSettings();

    /**
     * @return whether projectile is sticked to entity or block
     */
    boolean isSticked();

    /**
     * @return the sticked block
     */
    @Nullable
    Block getStickedBlock();

    /**
     * @return the sticked entity
     */
    @Nullable
    LivingEntity getStickedEntity();
}