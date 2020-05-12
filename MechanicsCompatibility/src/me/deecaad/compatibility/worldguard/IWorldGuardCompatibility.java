package me.deecaad.compatibility.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public interface IWorldGuardCompatibility {

    // todo: add docs
    boolean testFlag(Location location, @Nullable Player player, String flagName);

    // todo: add docs
    void registerFlags(String... flags);

    boolean isInstalled();
}