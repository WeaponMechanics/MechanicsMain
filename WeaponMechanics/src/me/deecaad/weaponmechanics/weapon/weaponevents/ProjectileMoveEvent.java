package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import org.bukkit.event.Cancellable;
import org.bukkit.util.Vector;

public class ProjectileMoveEvent extends ProjectileEvent implements Cancellable {

    private Vector path;
    private boolean isCancelled;

    public ProjectileMoveEvent(ICustomProjectile projectile, Vector path) {
        super(projectile);

        this.path = path;
    }

    public ICustomProjectile getProjectile() {
        return projectile;
    }

    public Vector getPath() {
        return path;
    }

    public void setPath(Vector path) {
        this.path = path;
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
