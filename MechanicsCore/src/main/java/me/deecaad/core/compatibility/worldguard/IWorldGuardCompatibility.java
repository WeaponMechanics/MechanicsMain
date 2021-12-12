package me.deecaad.core.compatibility.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * This interface outlines an API to use different versions of
 * <a href="https://dev.bukkit.org/projects/worldguard">World Guard</a>.
 */
public interface IWorldGuardCompatibility {

    /**
     * Tests the state flag with the given <code>flagName</code>. This method
     * will return <code>true</code> if that flag is enabled for the given
     * <code>location</code>.
     *
     * <p>If the <code>player</code> argument is not <code>null</code>, this
     * method can also return <code>true</code> if they have permission to
     * override a region's permission. For example, region owners can bypass
     * most, if not all restrictions.
     *
     * @param location The non-null coordinates to test for regions.
     * @param player   The player involved, or <code>null</code>.
     * @param flagName The non-null name of the flag.
     * @return <code>true</code> if the flag is enabled.
     */
    boolean testFlag(@Nonnull Location location, @Nullable Player player, @Nonnull String flagName);

    /**
     * Returns the value of a flag with the given <code>flagName</code>. The
     * returned value depends on the region that contains the
     * <code>location</code>.
     *
     * <p>The datatype of the returned value depends on the {@link FlagType}.
     *
     * @param location The non-null coordinates to test for regions.
     * @param flagName The non-null name of the flag.
     * @return The value of the flag for that region, or <code>null</code>.
     */
    @Nullable
    Object getValue(@Nonnull Location location, @Nonnull String flagName);

    /**
     * Registers a new flag with the given name and datatype.
     *
     * @param flag The non-null flag name.
     * @param type The non-null data type of the flag.
     * @throws NullPointerException If type is <code>null</code>.
     */
    void registerFlag(@Nonnull String flag, @Nonnull FlagType type);

    /**
     * Returns <code>true</code> if world guard is installed. This is the same
     * as <code>PluginManager#getPlugin("WorldGuard") != null</code>.
     *
     * @return <code>true</code> if the server uses world guard.
     */
    boolean isInstalled();

    /**
     * Returns a keySet from the {@link java.util.Map} of register flags. Only
     * flags added using {@link #registerFlag(String, FlagType)} are added to
     * the backing map.
     *
     * @return The non-null set of flag names.
     */
    @Nonnull
    Set<String> getRegisteredFlags();

    /**
     * This enum outlines the different data types that a flag can store.
     */
    enum FlagType {
        STATE_FLAG,
        DOUBLE_FLAG,
        INT_FLAG,
        STRING_FLAG,
    }
}