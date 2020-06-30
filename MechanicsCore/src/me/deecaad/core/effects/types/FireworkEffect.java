package me.deecaad.core.effects.types;

import me.deecaad.core.effects.Effect;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FireworkEffect extends Effect {



    @Override
    protected void spawnOnce(@Nonnull Plugin source, @Nonnull World world, double x, double y, double z, @Nullable Object data) {

    }

    @Override
    protected void spawnOnceFor(@Nonnull Plugin source, @Nonnull Player player, @Nonnull World world, double x, double y, double z, @Nullable Object data) {

    }
}
