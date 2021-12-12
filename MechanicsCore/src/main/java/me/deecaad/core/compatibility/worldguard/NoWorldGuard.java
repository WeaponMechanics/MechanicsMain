package me.deecaad.core.compatibility.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Used when WorldGuard is not installed
 */
public class NoWorldGuard implements IWorldGuardCompatibility {

    @Override
    public boolean testFlag(Location location, @Nullable Player player, String flagName) {
        return true;
    }

    @Override
    public Object getValue(@Nonnull Location location, @Nonnull String flagName) {
        return null;
    }

    @Override
    public void registerFlag(String flag, FlagType type) {
    }

    @Override
    public boolean isInstalled() {
        return false;
    }

    @Override
    public Set<String> getRegisteredFlags() {
        return Collections.emptySet();
    }
}
