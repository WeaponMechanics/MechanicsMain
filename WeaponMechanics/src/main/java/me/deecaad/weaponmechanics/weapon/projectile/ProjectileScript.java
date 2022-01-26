package me.deecaad.weaponmechanics.weapon.projectile;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import javax.annotation.Nonnull;

public abstract class ProjectileScript<T extends AProjectile> {

    protected T projectile;

    /**
     * Called while the projectile is spawning. It is guaranteed that this
     * method will be called before all other methods in this class. This
     * method is called after the projectile is shot, so getting the shooter
     * (for WeaponProjectile) is a safe operation.
     *
     * @return <code>true</code> will remove the projectile and unregister it.
     */
    public abstract boolean onStart();

    /**
     * Called towards the beginning of {@link AProjectile#tick()}. This method
     * <i>probably</i> should be used instead of {@link #onTickEnd()} when you
     * are modifying projectile values (Changing speed/location, for example).
     *
     * @return <code>true</code> will remove the projectile and unregister it.
     */
    public abstract boolean onTickStart();

    /**
     * Called towards the end of {@link AProjectile#tick()}. This method
     * <i>probably</i> should be used instead of {@link #onTickStart()} when
     * you only want to read the result of a tick method (See how a projectile
     * moved, for example).
     *
     * @return <code>true</code> will remove the projectile and unregister it.
     */
    public abstract boolean onTickEnd();

    /**
     * Called when the projectile is removed.
     *
     * @return <code>true</code> will remove the projectile and unregister it.
     */
    public abstract boolean onEnd();

    /**
     * Called when the projectile collides with a block. Note that projectiles
     * are allowed to skip collision checks.
     *
     * @param block The non-null block.
     * @return <code>true</code> will remove the projectile and unregister it.
     */
    public abstract boolean onCollide(@Nonnull Block block);

    /**
     * Called when the projectile collides with an entity. Note that
     * projectiles are allowed to skip collision checks.
     *
     * @param entity The non-null entity.
     * @return <code>true</code> will remove the projectile and unregister it.
     */
    public abstract boolean onCollide(@Nonnull Entity entity);
}
