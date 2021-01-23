package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.utils.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * Class to handle all projectiles ticking
 */
public class CustomProjectilesRunnable extends BukkitRunnable {

    // Linked list for O(1) Iterator#remove, and #add
    private LinkedList<ICustomProjectile> projectiles;
    private AtomicReference<ICustomProjectile[]> asyncProjectiles;

    /**
     * Initializes custom projectiles runnable
     *
     * @param plugin the plugin used to run task
     */
    public CustomProjectilesRunnable(Plugin plugin) {
        projectiles = new LinkedList<>();
        asyncProjectiles = new AtomicReference<>(new ICustomProjectile[0]);

        runTaskTimer(plugin, 0, 0);
    }

    /**
     * Adds the given <code>projectile</code> to the list of <code>projectiles</code>
     * that are currently being ticked. This method should be threadsafe.
     *
     * @param projectile The projectile to add
     */
    public void addProjectile(ICustomProjectile projectile) {

        // If were are on the main server thread, then we know
        // that the projectiles aren't currently being ticked
        if (Bukkit.getServer().isPrimaryThread()) {
            projectiles.add(projectile);
            return;
        }

        // We have to do the work over and over again until we
        // successfully set the value. It is highly unlikely that
        // this loop will occur >2 times from any 1 call. Should
        // still be significantly faster then trying to obtain a lock
        boolean isSuccess = false;
        while (!isSuccess) {

            // AtomicReferences work with immutable objects. Here, we
            // "fake" this immutability by creating a new array and copying
            // the elements every time we want to add a new element
            ICustomProjectile[] immutableArr = asyncProjectiles.get();
            ICustomProjectile[] copy = new ICustomProjectile[immutableArr.length + 1];
            System.arraycopy(immutableArr, 0, copy, 0, immutableArr.length);

            // Add the new projectile
            copy[immutableArr.length] = projectile;

            // Try to set the reference, and determine if we are successful
            isSuccess = asyncProjectiles.compareAndSet(immutableArr, copy);
        }
    }

    /**
     * Adds all of the given <code>projectiles</code> to the internal list of
     * <code>projectiles</code> that are currently being ticked. This method
     * should be threadsafe.
     *
     * @param projectiles The projectiles to add
     */
    public void addProjectiles(List<ICustomProjectile> projectiles) {

        // If were are on the main server thread, then we know
        // that the projectiles aren't currently being ticked
        if (Bukkit.getServer().isPrimaryThread()) {
            this.projectiles.addAll(projectiles);
            return;
        }

        // We have to do the work over and over again until we
        // successfully set the value. It is highly unlikely that
        // this loop will occur >2 times from any 1 call. Should
        // still be significantly faster then trying to obtain a lock
        boolean isSuccess = false;
        while (!isSuccess) {

            // AtomicReferences work with immutable objects. Here, we
            // "fake" this immutability by creating a new array and copying
            // the elements every time we want to add a new element
            ICustomProjectile[] immutableArr = asyncProjectiles.get();
            ICustomProjectile[] copy = new ICustomProjectile[immutableArr.length + projectiles.size()];
            System.arraycopy(immutableArr, 0, copy, 0, immutableArr.length);

            // Add the new projectiles
            for (int i = 0; i < projectiles.size(); i++) {
                copy[immutableArr.length + i] = projectiles.get(i);
            }

            // Try to set the reference, and determine if we are successful
            isSuccess = asyncProjectiles.compareAndSet(immutableArr, copy);
        }

    }

    /**
     * This method will always be run on the main server thread
     */
    @Override
    public void run() {

        boolean isSuccess = false;
        while (!isSuccess) {

            // Get the projectiles that were added async, and add
            // all of them to the sync projectile list
            ICustomProjectile[] immutableArr = asyncProjectiles.get();
            projectiles.addAll(Arrays.asList(immutableArr));

            // Attempt to empty the asyncProjectiles list
            isSuccess = asyncProjectiles.compareAndSet(immutableArr, new ICustomProjectile[0]);
        }

        Iterator<ICustomProjectile> projectilesIterator = projectiles.iterator();

        while (projectilesIterator.hasNext()) {
            try {
                ICustomProjectile projectile = projectilesIterator.next();
                if (false) {
                    projectilesIterator.remove();
                }
            } catch (Exception e) {
                projectilesIterator.remove();
                debug.log(LogLevel.WARN, "Unhandled exception while ticking projectiles", e);
            }
        }
    }
}