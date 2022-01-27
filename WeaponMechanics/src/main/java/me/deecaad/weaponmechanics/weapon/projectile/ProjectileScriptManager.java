package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public abstract class ProjectileScriptManager {

    private final Plugin plugin;

    public ProjectileScriptManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public abstract void attach(@Nonnull AProjectile projectile);

    public void register() {
        WeaponMechanics.getProjectilesRunnable().addScriptManager(this);
    }
}
