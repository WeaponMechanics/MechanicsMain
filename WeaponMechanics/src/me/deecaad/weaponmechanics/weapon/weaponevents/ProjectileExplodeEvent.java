package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import org.bukkit.event.Cancellable;

public class ProjectileExplodeEvent extends ProjectileEvent implements Cancellable {

    private Explosion explosion;
    private boolean isCancelled;

    public ProjectileExplodeEvent(ICustomProjectile projectile, Explosion explosion) {
        super(projectile);
        this.explosion = explosion;
    }

    public Explosion getExplosion() {
        return explosion;
    }

    public void setExplosion(Explosion explosion) {
        this.explosion = explosion;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
