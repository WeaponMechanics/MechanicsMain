package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.inline.types.StringType;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorldTargeter extends Targeter {

    public static final Argument WORLD = new Argument("world", new StringType(), null);

    private final String worldName;
    private World worldCache;

    public WorldTargeter(Map<Argument, Object> args) throws InlineException {
        super(args);
        worldName = (String) args.get(WORLD);
    }

    @Override
    public ArgumentMap args() {
        return super.args().addAll(WORLD);
    }

    @Override
    public String getKeyword() {
        return "World";
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    public List<CastData> getTargets0(CastData cast) {
        if (worldCache == null || worldName == null)
            worldCache = worldName == null ? cast.getSource().getWorld() : Bukkit.getWorld(worldName);

        // User may have typed the name of the world wrong... It is case-sensitive
        if (worldCache == null) {
            MechanicsCore.debug.warn("There was an error getting the world for '" + worldName  + "'");
            return List.of();
        }

        // Loop through every living entity in the world
        List<CastData> targets = new LinkedList<>();
        for (LivingEntity target : worldCache.getLivingEntities()) {
            CastData copy = cast.clone();
            copy.setTargetEntity(target);
            targets.add(copy);
        }

        return targets;
    }
}
