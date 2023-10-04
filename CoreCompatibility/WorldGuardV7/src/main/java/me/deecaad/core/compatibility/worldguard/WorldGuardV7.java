package me.deecaad.core.compatibility.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class WorldGuardV7 implements WorldGuardCompatibility {

    private final Map<String, Flag<?>> flags;
    private final FlagRegistry registry;

    public WorldGuardV7() {
        flags = new HashMap<>();
        registry = WorldGuard.getInstance().getFlagRegistry();
    }

    @Override
    public boolean testFlag(@NotNull Location location, @Nullable Player player, @NotNull String flagName) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(BukkitAdapter.adapt(location));
        LocalPlayer local = player == null ? null : WorldGuardPlugin.inst().wrapPlayer(player);

        Flag<?> flag = flags.get(flagName);
        if (flag == null) {
            String flagList = "[" + String.join(", ", flags.keySet()) + "]";
            throw new IllegalArgumentException("Unknown flag: " + flagName + ", Available: " + flagList);
        } else if (!(flag instanceof StateFlag)) {
            throw new IllegalArgumentException("Flag: " + flagName + " is not a StateFlag");
        }

        StateFlag stateFlag = (StateFlag) flag;
        return applicableRegionSet.testState(local, stateFlag);
    }

    @Override
    public Object getValue(@NotNull Location location, @NotNull String flagName) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(BukkitAdapter.adapt(location));

        Flag<?> flag = flags.get(flagName);
        if (flag == null) {
            String flagList = "[" + String.join(", ", flags.keySet()) + "]";
            throw new IllegalArgumentException("Unknown flag: " + flagName + ", Available: " + flagList);
        }

        return applicableRegionSet.queryValue(null, flag);
    }

    @Override
    public void registerFlag(@NotNull String flagString, @NotNull FlagType type) {
        Flag<?> flag = switch (type) {
            case INT_FLAG -> new IntegerFlag(flagString);
            case STATE_FLAG -> new StateFlag(flagString, true);
            case DOUBLE_FLAG -> new DoubleFlag(flagString);
            case STRING_FLAG -> new StringFlag(flagString);
        };

        // An illegal argument exception will occur here if the flag
        // name is invalid. A valid flag matches: "^[:A-Za-z0-9\\-]{1,40}$"

        try {
            registry.register(flag);
        } catch (FlagConflictException ex) {

            // At this point, we know that there is already a world guard
            // flag with this name, so we should get it
            Flag<?> existing = registry.get(flagString);

            // Check if the data type of the 2 flags are the same, and
            // try to use it
            if (existing.getClass() == flag.getClass()) {
                Bukkit.getLogger().log(Level.WARNING, "MechanicsCore is borrowing the WorldGuard flag: " + flagString);
                flag = existing;
            } else {
                throw new IllegalStateException(flagString + " is already registered!");
            }
        }

        flags.put(flagString, flag);
    }

    @Override
    public @NotNull Set<String> getRegisteredFlags() {
        return flags.keySet();
    }

    @Override
    public boolean isInstalled() {
        return true;
    }
}