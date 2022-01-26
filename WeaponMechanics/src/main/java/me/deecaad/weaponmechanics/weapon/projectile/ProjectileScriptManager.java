package me.deecaad.weaponmechanics.weapon.projectile;

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
}
