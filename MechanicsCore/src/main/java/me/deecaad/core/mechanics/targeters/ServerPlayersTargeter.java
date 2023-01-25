package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class ServerPlayersTargeter extends Targeter {

    /**
     * Default constructor for serializer.
     */
    public ServerPlayersTargeter() {
    }

    @Override
    public String getKeyword() {
        return "Server";
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    public List<CastData> getTargets0(CastData cast) {

        // Loop through every player in the server
        List<CastData> targets = new LinkedList<>();
        for (LivingEntity target : Bukkit.getServer().getOnlinePlayers()) {
            CastData copy = cast.clone();
            copy.setTargetEntity(target);
            targets.add(copy);
        }

        return targets;
    }

    @NotNull
    @Override
    public Targeter serialize(SerializeData data) throws SerializerException {
        String worldName = data.of("World").assertType(String.class).get(null);
        return applyParentArgs(data, new WorldTargeter(worldName));
    }
}
