package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class ProjectileHitEntityEvent extends ProjectileEvent {

    private final LivingEntity entity;
    private final Vector exactLocation;
    private DamagePoint point;
    private boolean isBackStab;

    public ProjectileHitEntityEvent(ICustomProjectile projectile, LivingEntity entity, Vector exactLocation, DamagePoint point, boolean isBackStab) {
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
}
