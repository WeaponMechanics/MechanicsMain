package me.deecaad.compatibility.worldguard;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class WorldGuardV6 implements IWorldGuardCompatibility {

    @Override
    public boolean testFlag(Location location, @Nullable Player player, String flagName) {
        RegionManager regionManager = WorldGuardPlugin.inst().getRegionManager(location.getWorld());
        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(location);
        LocalPlayer local = player == null ? null : WorldGuardPlugin.inst().wrapPlayer(player);

        return false;
    }

    @Override
    public void registerFlags(String... flags) {

    }
}
