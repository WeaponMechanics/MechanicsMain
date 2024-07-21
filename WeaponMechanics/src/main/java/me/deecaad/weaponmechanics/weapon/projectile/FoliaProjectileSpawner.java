package me.deecaad.weaponmechanics.weapon.projectile;

import com.tcoded.folialib.impl.PlatformScheduler;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Spawns projectiles each on their own thread, in the region they are spawned in.
 */
public class FoliaProjectileSpawner extends ProjectileSpawner {

    /**
     * Initializes the spawner with the given plugin.
     *
     * @param plugin The non-null plugin used to register runnables
     */
    public FoliaProjectileSpawner(@NotNull Plugin plugin) {
        super(plugin);
        WeaponMechanics.debug.log(LogLevel.INFO, "Using FoliaProjectileSpawner");
    }

    @Override
    public void spawn(@NotNull AProjectile projectile) {
        PlatformScheduler scheduler = MechanicsCore.getPlugin().getFoliaScheduler();
        Location location = projectile.getBukkitLocation();

        // We cannot tick this projectile, so we need to switch threads to spawn it
        if (!projectile.isOwnedByCurrentRegion()) {
            scheduler.runAtLocation(location, (task) -> spawn0(scheduler, location, projectile));
            return;
        }

        spawn0(scheduler, location, projectile);
    }

    private void spawn0(
        @NotNull PlatformScheduler scheduler,
        @NotNull Location location,
        @NotNull AProjectile projectile
    ) {
        if (!projectile.isOwnedByCurrentRegion())
            throw new IllegalStateException("Projectile is not owned by the current region");

        // Attach projectile managers, do first tick, and return if the projectile already died
        if (doFirstTick(projectile))
            return;

        scheduler.runAtLocationTimer(location, (task) -> {
            // Ideally, this would never happen.
            if (!projectile.isOwnedByCurrentRegion()) {
                WeaponMechanics.debug.log(LogLevel.WARN, "Projectile is not owned by the current region",
                    "This might happen if a projectile slowly moved out of the region while ticking.");
                //projectile.remove();
                task.cancel();
                // TODO schedule new task in region?
            }

            if (projectile.isDead()) {
                task.cancel();
                return;
            }

            try {
                if (projectile.tick()) {
                    projectile.remove();
                    task.cancel();
                }
            } catch (Throwable ex) {
                WeaponMechanics.debug.log(LogLevel.WARN, "An unhandled exception occurred while ticking a projectile", ex);
                projectile.remove();
                task.cancel();
            }
        }, 0L, 0L);

    }
}
