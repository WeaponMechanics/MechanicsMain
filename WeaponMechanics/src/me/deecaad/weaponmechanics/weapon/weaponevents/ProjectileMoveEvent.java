package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.util.Vector;

public class ProjectileMoveEvent extends WeaponEvent implements Cancellable {

    private CustomProjectile projectile;
    private Vector path;
    private boolean isCancelled;

    public ProjectileMoveEvent(CustomProjectile projectile,) {
        super(projectile.getTag("weaponTitle"), projectile.getShooter());

        this.projectile = projectile;
        this.isCancelled = isCancelled;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    public Location getLastLocation() {
        Vector vector = projectile.getLastLocation();
        return new Location(projectile.getWorld(), vector.getX(), vector.getY(), vector.getZ());
    }

    public Location getLocation() {
        Vector vector = projectile.getLocation();
        return new Location(projectile.getWorld(), vector.getX(), vector.getY(), vector.getZ());
    }

    public Vector getPath() {
        return projectile.getLocation().subtract(projectile.getLastLocation());
    }


}
