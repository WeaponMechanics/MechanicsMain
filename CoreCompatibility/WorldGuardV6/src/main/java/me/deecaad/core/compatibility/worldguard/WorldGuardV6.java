package me.deecaad.core.compatibility.worldguard;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class WorldGuardV6 implements IWorldGuardCompatibility {

    private final Map<String, Flag<?>> flags;
    private final FlagRegistry registry;

    public WorldGuardV6() {
        flags = new HashMap<>();
        registry = WorldGuardPlugin.inst().getFlagRegistry();
    }

    @Override
    public boolean testFlag(Location location, @Nullable Player player, String flagName) {
        RegionManager regionManager = WorldGuardPlugin.inst().getRegionManager(location.getWorld());
        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(location);
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
    public Object getValue(@Nonnull Location location, @Nonnull String flagName) {
        RegionManager regionManager = WorldGuardPlugin.inst().getRegionManager(location.getWorld());
        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(location);

        Flag<?> flag = flags.get(flagName);
        if (flag == null) {
            String flagList = "[" + String.join(", ", flags.keySet()) + "]";
            throw new IllegalArgumentException("Unknown flag: " + flagName + ", Available: " + flagList);
        }

        return applicableRegionSet.queryValue(null, flag);
    }

    @Override
    public void registerFlag(String flagString, FlagType type) {
        Flag<?> flag;

        // An illegal argument exception will occur here if the flag
        // name is invalid. A valid flag matches: "^[:A-Za-z0-9\\-]{1,40}$"
        switch (type) {
            case INT_FLAG:
                flag = new IntegerFlag(flagString);
                break;
            case STATE_FLAG:
                flag = new StateFlag(flagString, false);
                break;
            case DOUBLE_FLAG:
                flag = new DoubleFlag(flagString);
                break;
            case STRING_FLAG:
                flag = new StringFlag(flagString);
                break;
            default:
                throw new IllegalArgumentException("Unknown FlagType " + type);
        }

        try {
            registry.register(flag);
        } catch (FlagConflictException ex) {
            Flag<?> existing = registry.get(flagString);

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
        return false;
    }
}
