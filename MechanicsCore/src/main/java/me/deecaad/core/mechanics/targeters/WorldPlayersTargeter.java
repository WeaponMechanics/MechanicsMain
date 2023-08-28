package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Iterator;

/**
 * Targets every player in the current world. This targeter is not shown to
 * players. Whenever the player uses the @World{} targeter with a player-only
 * mechanic (or use a condition for players only), this targeter will be used
 * instead. This has major performance benefits.
 */
public class WorldPlayersTargeter extends WorldTargeter {

    /**
     * Empty constructor for serializer.
     */
    public WorldPlayersTargeter() {
    }

    public WorldPlayersTargeter(String worldName) {
        super(worldName);
    }

    @Override
    public String getKeyword() {
        return "WorldPlayers";
    }

    @Override
    public Iterator<CastData> getTargets0(CastData cast) {
        if (getWorldCache() == null || getWorldName() == null)
            setWorldCache(getWorldName() == null ? cast.getSource().getWorld() : Bukkit.getWorld(getWorldName()));

        // User may have typed the name of the world wrong... It is case-sensitive
        if (getWorldCache() == null) {
            MechanicsCore.debug.warn("There was an error getting the world for '" + getWorldName()  + "'");
            return Collections.emptyIterator();
        }

        // Loop through every living entity in the world
        Iterator<Player> entityIterator = getWorldCache().getPlayers().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return entityIterator.hasNext();
            }

            @Override
            public CastData next() {
                cast.setTargetEntity(entityIterator.next());
                return cast;
            }
        };
    }
}
