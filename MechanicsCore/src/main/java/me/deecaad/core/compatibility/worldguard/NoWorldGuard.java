package me.deecaad.core.compatibility.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * Used when WorldGuard is not installed
 */
public class NoWorldGuard implements WorldGuardCompatibility {

    @Override
    public boolean testFlag(@NotNull Location location, @Nullable Player player, @NotNull String flagName) {
        return true;
    }

    @Override
    public Object getValue(@NotNull Location location, @NotNull String flagName) {
        return null;
    }

    @Override
    public void registerFlag(@NotNull String flag, @NotNull FlagType type) {
    }

    @Override
    public boolean isInstalled() {
        return false;
    }

    @Override
    public @NotNull Set<String> getRegisteredFlags() {
        return Collections.emptySet();
    }
}
