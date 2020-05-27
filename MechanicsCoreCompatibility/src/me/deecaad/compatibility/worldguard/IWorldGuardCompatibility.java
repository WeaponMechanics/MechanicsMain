package me.deecaad.compatibility.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public interface IWorldGuardCompatibility {

    /**
     * Gets the <code>StateFlag</code> based on the given flagName
     * and tests it against the given player at the region found at
     * the given <code>Location</code>
     *
     * @param location Where to test
     * @param player Who to test against (Ops have more permission, for example)
     * @param flagName The name of the flag to test
     * @return true if the flag is enabled in the region
     */
    boolean testFlag(Location location, @Nullable Player player, String flagName);

    /**
     * Gets the value of the <code>Flag</code> with the given flagName
     * in the region found at the given <code>Location</code>
     *
     * @param location Where to get the value
     * @param flagName The name of the Flag to use
     * @return The value of the flag
     */
    Object getValue(Location location, String flagName);

    /**
     * Registers a< <code>Flag</code> with the given name and type
     *
     * @param flag The name of the flag
     * @param type Which type the flag should be
     */
    void registerFlag(String flag, FlagType type);

    /**
     * @return true if WorldGuard is installed
     */
    boolean isInstalled();

    public static enum FlagType {

        STATE_FLAG,
        DOUBLE_FLAG,
        INT_FLAG,
        STRING_FLAG,

    }
}