package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * This class outlines the event of a projectile hitting a
 * {@link LivingEntity}. If this event is cancelled, the projectile will not
 * <i>interact</i> with the entity (Cancelling damage, explosions, mechanics,
 * etc.)
 */
public class ProjectileHitEntityEvent extends ProjectileEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity entity;
    private final Vector exactLocation;
    private DamagePoint point;
    private boolean isBackStab;
    private boolean isCancelled;

    public ProjectileHitEntityEvent(WeaponProjectile projectile, LivingEntity entity, Vector exactLocation, DamagePoint point, boolean isBackStab) {
        super(projectile);
        this.entity = entity;
        this.exactLocation = exactLocation;
        this.point = point;
        this.isBackStab = isBackStab;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public EntityType getEntityType() {
        return entity.getType();
    }

    public Location getHitLocation() {
        return exactLocation.toLocation(projectile.getWorld());
    }

    public DamagePoint getPoint() {
        return point;
    }

    public void setPoint(@Nullable DamagePoint point) {
        this.point = point;
    }

    public boolean isBackStab() {
        return isBackStab;
    }

    public void setBackStab(boolean backStab) {
        isBackStab = backStab;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
