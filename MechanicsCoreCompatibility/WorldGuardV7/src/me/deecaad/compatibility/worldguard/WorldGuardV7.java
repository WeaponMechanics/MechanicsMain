package me.deecaad.compatibility.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.netty.handler.logging.LogLevel;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Map;

public class WorldGuardV7 implements IWorldGuardCompatibility {

    private Map<String, StateFlag> flags;

    public WorldGuardV7() {

    }

    @Override
    public boolean testFlag(Location location, @Nullable Player player, String flagName) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(BukkitAdapter.adapt(location));
        LocalPlayer local = player == null ? null : WorldGuardPlugin.inst().wrapPlayer(player);

        StateFlag flag = flags.get(flagName);
        if (flag == null) {
            String flagList = "[" + String.join(", ", flags.keySet()) + "]";
            //debug.log(LogLevel.ERROR, "Flag \"" + flagName + "\" does not exist...", "Available flags: " + flagList);
        }

        return false;
    }

    @Override
    public void registerFlags(String... flags) {

    }

    @Override
    public boolean isInstalled() {
        return true;
    }
}