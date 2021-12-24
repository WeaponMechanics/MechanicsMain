package me.deecaad.weaponmechanics.weapon.projectile;

import co.aikar.timings.lib.MCTiming;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
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

    /**
     * Initializes and registers this runnable. This runnable can be cancelled
     * using {@link #cancel()} or by cancelling all tasks for code>plugin</code>
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

        runTaskTimer(plugin, 0, 0);
    }

    /**
     * Adds the given projectile to be ticked starting during the next tick.
     * This method is threadsafe, and you may call this method async.
     *
     * @param projectile The non-null projectile to tick.
     */
    public void addProjectile(AProjectile projectile) {
        if (projectile == null)
            throw new IllegalArgumentException("Cannot add null projectile!");

        if (Bukkit.getServer().isPrimaryThread()) {
            projectiles.add(projectile);
            return;
        }

        asyncProjectiles.add(projectile);
    }

    /**
     * Adds the given projectiles to be ticked starting during the next tick.
     * This method is threadsafe, and you may call this method async.
     *
     * @param projectiles The non-null collection of non-null projectiles.
     */
    public void addProjectiles(Collection<? extends AProjectile> projectiles) {
        if (projectiles.contains(null))
            throw new IllegalArgumentException("Cannot add null projectiles");

        if (Bukkit.getServer().isPrimaryThread()) {
            this.projectiles.addAll(projectiles);
            return;
        }

        asyncProjectiles.addAll(projectiles);
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
            projectiles.add(asyncProjectiles.remove());
        }

        Iterator<AProjectile> projectilesIterator = projectiles.iterator();

        // Start timings for general projectile ticking. We may consider
        // separating projectiles by weapon-title, but that would require more
        // resources
        MCTiming timing = WeaponMechanics.timing("Scheduled Projectiles");
        timing.startTiming();

        while (projectilesIterator.hasNext()) {
            AProjectile projectile = projectilesIterator.next();
            try {

                if (projectile.tick()) {
                    // todo Bukkit.getPluginManager().callEvent(new ProjectileEndEvent(projectile));
                    projectilesIterator.remove();
                }
            } catch (Exception e) {
                projectilesIterator.remove();
                debug.log(LogLevel.WARN, "Unhandled exception while ticking projectiles! Removing projectile");
                debug.log(LogLevel.WARN, "Removed Projectile: " + projectile, e);
            }
        }

        // End timings for projectile ticking
        timing.stopTiming();
    }
}