package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class ServerPlayersTargeter extends Targeter {

    /**
     * Default constructor for serializer.
     */
    public ServerPlayersTargeter() {
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    public Iterator<CastData> getTargets0(CastData cast) {
        Iterator<? extends Player> playerIterator = Bukkit.getServer().getOnlinePlayers().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return playerIterator.hasNext();
            }

            @Override
            public CastData next() {
                Player target = playerIterator.next();
                cast.setTargetEntity(target);
                return cast;
            }
        };
    }

    @Override
    public String getKeyword() {
        return "Server_Players";
    }

    @Nullable
    @Override
    public String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/targeters/serverplayers";
    }

    @NotNull
    @Override
    public Targeter serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new ServerPlayersTargeter());
    }
}
