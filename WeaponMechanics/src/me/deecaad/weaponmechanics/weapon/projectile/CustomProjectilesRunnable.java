package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.utils.LogLevel;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * Class to handle all projectiles ticking
 */
public class CustomProjectilesRunnable extends BukkitRunnable {

    private static Set<CustomProjectile> projectiles;

    /**
     * Initializes custom projectiles runnable
     *
     * @param plugin the plugin used to run task
     * @param async  whether or not to run projectiles in async
     */
    public void init(Plugin plugin, boolean async) {

        if (projectiles != null) {
            debug.log(LogLevel.ERROR, plugin.getName() + " tried to initialize custom projectiles runnable.",
                    "Can not initialize multiple times...");
            return;
        }

        if (async) {
            projectiles = ConcurrentHashMap.newKeySet();
            runTaskTimerAsynchronously(plugin, 0, 0);
        } else {
            projectiles = new HashSet<>();
            runTaskTimer(plugin, 0, 0);
        }
    }

    public static void addProjectile(CustomProjectile projectile) {
        projectiles.add(projectile);
    }

    @Override
    public void run() {
        if (projectiles.isEmpty()) {
            return;
        }
        Iterator<CustomProjectile> projectilesIterator = projectiles.iterator();

        while (projectilesIterator.hasNext()) {
            try {
                if (projectilesIterator.next().tick()) {
                    projectilesIterator.remove();
                }

            } catch (Exception e) {
                projectilesIterator.remove();
                debug.log(LogLevel.WARN, "Caught exception during ticking custom projectiles!", e);
            }
        }
    }
}
