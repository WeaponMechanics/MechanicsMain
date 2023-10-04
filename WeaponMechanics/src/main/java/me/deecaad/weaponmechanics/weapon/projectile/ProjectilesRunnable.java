package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * This class is run once for every Minecraft Server tick (ideally 20 ticks per
 * second). This class stores the entities we need to tick, and the methods of
 * this class are thread safe. The order in which the projectiles are ticked
 * is undefined, but every projectile is guaranteed to tick once for every MC
 * server tick.
 */
public class ProjectilesRunnable extends BukkitRunnable {

    private final LinkedList<AProjectile> projectiles;
    private final LinkedBlockingQueue<AProjectile> asyncProjectiles;
    private final List<ProjectileScriptManager> managers;

    /**
     * Initializes and registers this runnable. This runnable can be cancelled
     * using {@link #cancel()} or by cancelling all tasks for <code>plugin</code>
     * using <code>Bukkit.getScheduler().cancelTasks(plugin)</code>.
     *
     * <p> WeaponMechanics initializes one of these by default. You probably
     * do not want to instantiate this class unless you know what you are
     * doing. Use {@link WeaponMechanics#getProjectilesRunnable()}.
     *
     * @param plugin The non-null plugin
     */
    public ProjectilesRunnable(Plugin plugin) {
        projectiles = new LinkedList<>();
        asyncProjectiles = new LinkedBlockingQueue<>();
        managers = new LinkedList<>();

        runTaskTimer(plugin, 0, 0);
    }

    public void addScriptManager(@NotNull ProjectileScriptManager manager) {
        managers.add(manager);
    }

    /**
     * Adds the given projectiles to be ticked. Projectile is instantly
     * ticked once. On async call ticking starts during the next tick.
     * This method is threadsafe, and you may call this method async.
     *
     * @param projectile The non-null projectile to tick.
     */
    public void addProjectile(@NotNull AProjectile projectile) {
        if (projectile == null)
            throw new IllegalArgumentException("Cannot add null projectile!");

        if (projectile.isDead()) return;

        if (Bukkit.getServer().isPrimaryThread()) {
            tickOnAdd(projectile);
            return;
        }

        asyncProjectiles.add(projectile);
    }

    /**
     * Adds the given projectiles to be ticked. Projectile is instantly
     * ticked once. On async call ticking starts during the next tick.
     * This method is threadsafe, and you may call this method async.
     *
     * @param projectiles The non-null collection of non-null projectiles.
     */
    public void addProjectiles(Collection<? extends AProjectile> projectiles) {
        if (Bukkit.getServer().isPrimaryThread()) {
            for (AProjectile projectile : projectiles) {
                if (projectile == null || projectile.isDead()) continue;

                tickOnAdd(projectile);
            }
            return;
        }

        asyncProjectiles.addAll(projectiles);
    }

    private void tickOnAdd(@NotNull AProjectile projectile) {
        for (ProjectileScriptManager manager : managers) {
            manager.attach(projectile);
        }
        try {
            if (projectile.tick()) {

                // Call the remove method of projectile
                projectile.remove();
                return;
            }
        } catch (Exception e) {
            projectile.remove();
            debug.log(LogLevel.WARN, "Unhandled exception while ticking projectile! Removing projectile");
            debug.log(LogLevel.WARN, "Removed Projectile: " + projectile, e);
            return;
        }

        // Since code reached this point, projectile didn't hit anything instantly
        // -> Add to the normal runnable
        this.projectiles.add(projectile);
    }

    /**
     * This method will always be run on the main server thread
     */
    @Override
    public void run() {

        // Extra check in case somebody runs this method by their own call.
        if (!Bukkit.getServer().isPrimaryThread())
            throw new IllegalStateException("Cannot tick projectiles asynchronously!");

        // Clears the async projectiles WHILE adding them to the normal projectiles
        while (!asyncProjectiles.isEmpty()) {
            AProjectile asyncProjectile = asyncProjectiles.remove();
            projectiles.add(asyncProjectile);

            for (ProjectileScriptManager manager : managers)
                manager.attach(asyncProjectile);
        }

        Iterator<AProjectile> projectilesIterator = projectiles.iterator();

        while (projectilesIterator.hasNext()) {
            AProjectile projectile = projectilesIterator.next();
            try {
                if (projectile.tick()) {

                    // Call the remove method of projectile
                    projectile.remove();

                    // Remove the projectile from runnable
                    projectilesIterator.remove();
                }
            } catch (Exception e) {
                projectilesIterator.remove();
                debug.log(LogLevel.WARN, "Unhandled exception while ticking projectiles! Removing projectile");
                debug.log(LogLevel.WARN, "Removed Projectile: " + projectile, e);
            }
        }
    }
}
