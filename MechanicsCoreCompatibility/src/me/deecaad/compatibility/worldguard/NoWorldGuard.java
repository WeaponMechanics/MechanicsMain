package me.deecaad.compatibility.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/**
 * Used when WorldGuard is not installed
 */
public class NoWorldGuard implements IWorldGuardCompatibility {

    @Override
    public boolean testFlag(Location location, @Nullable Player player, String flagName) {
        // Always allow whatever this test flag even tries to check when WorldGuard isn't installed
        return true;
    }

    @Override
    public Object getValue(Location location, String flagName) {
        return null;
    }

    @Override
    public void registerFlag(String flag, FlagType type) {
        // Do nothing
    }


    @Override
    public boolean isInstalled() {
        return false;
    }
}
