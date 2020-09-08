package me.deecaad.compatibility.worldguard;

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
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static me.deecaad.core.MechanicsCore.debug;

public class WorldGuardV7 implements IWorldGuardCompatibility {

    private Map<String, Flag<?>> flags;
    private FlagRegistry registry;

    public WorldGuardV7() {
        flags = new HashMap<>();
        registry = WorldGuard.getInstance().getFlagRegistry();
    }

    @Override
    public boolean testFlag(Location location, @Nullable Player player, String flagName) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(BukkitAdapter.adapt(location));
        LocalPlayer local = player == null ? null : WorldGuardPlugin.inst().wrapPlayer(player);

        Flag<?> flag = flags.get(flagName);
        if (flag == null) {
            String flagList = "[" + String.join(", ", flags.keySet()) + "]";
            debug.error("Flag \"" + flagName + "\" does not exist...", "Available flags: " + flagList);
            return true;

        } else if (!(flag instanceof StateFlag)) {
            debug.error("Flag \"" + flagName + "\" is not a StateFlag!!!");
            return true;
        }

        StateFlag stateFlag = (StateFlag) flag;
        return applicableRegionSet.testState(local, stateFlag);
    }

    @Override
    public Object getValue(Location location, String flagName) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(BukkitAdapter.adapt(location));

        Flag<?> flag = flags.get(flagName);
        if (flag == null) {
            String flagList = "[" + String.join(", ", flags.keySet()) + "]";
            debug.error("Flag \"" + flagName + "\" does not exist...", "Available flags: " + flagList);
            return true;

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
                flag = new StateFlag(flagString, true);
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

            // At this point, we know that there is already a world guard
            // flag with this name, so we should get it
            Flag<?> existing = registry.get(flagString);

            // Check if the data type of the 2 flags are the same, and
            // try to use it
            if (existing.getClass() == flag.getClass()) {
                debug.warn("Flag " + flagString + " already registered. This may cause compatibility issues!");
                flag = existing;
            } else {
                debug.error("Incompatible flag types! " + flagString + " is already registered!!!");
            }
        }

        flags.put(flagString, flag);
    }

    @Override
    public Set<String> getRegisteredFlags() {
        return flags.keySet();
    }

    @Override
    public boolean isInstalled() {
        return true;
    }
}