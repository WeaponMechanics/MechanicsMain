package me.deecaad.compatibility.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class NoWorldGuard implements IWorldGuardCompatibility {

    @Override
    public boolean testFlag(Location location, @Nullable Player player, String flagName) {
        // Always allow whatever this test flag even tries to check when WorldGuard isn't installed
        return true;
    }

    @Override
    public void registerFlags(String... flags) {
        // Do nothing...
    }

    @Override
    public boolean isInstalled() {
        return false;
    }
}
