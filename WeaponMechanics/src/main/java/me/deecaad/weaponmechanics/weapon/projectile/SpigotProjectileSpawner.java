package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Spawns projectiles onto 1 central thread.
 */
public class SpigotProjectileSpawner extends ProjectileSpawner implements Runnable {

    private final List<AProjectile> projectiles;

    /**
     * Initializes the spawner with the given plugin.
     *
     * @param plugin The non-null plugin used to register runnables
     */
    public SpigotProjectileSpawner(@NotNull Plugin plugin) {
        super(plugin);
        this.projectiles = new LinkedList<>();
    }

    @Override
    public void spawn(@NotNull AProjectile projectile) {
        if (!projectile.isOwnedByCurrentRegion()) {
            Bukkit.getScheduler().runTask(plugin, () -> spawn0(projectile));
            return;
        }

        spawn0(projectile);
    }

    private void spawn0(@NotNull AProjectile projectile) {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("This method must be called on the primary thread");

        // Attach projectile managers, do first tick, and return if the projectile already died
        if (doFirstTick(projectile))
            return;

        projectiles.add(projectile);
    }

    @Override
    public void run() {
        if (!Bukkit.getServer().isPrimaryThread())
            throw new IllegalStateException("This method must be called on the primary thread");

        Iterator<AProjectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            AProjectile projectile = iterator.next();
            if (projectile.isDead()) {
                projectile.remove();
                iterator.remove();
                continue;
            }

            try {
                if (projectile.tick()) {
                    projectile.remove();
                    iterator.remove();
                }
            } catch (Throwable ex) {
                WeaponMechanics.debug.log(LogLevel.WARN, "An unhandled exception occurred while ticking a projectile", ex);
                projectile.remove();
                iterator.remove();
            }
        }
    }
}
