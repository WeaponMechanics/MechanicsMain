package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightningMechanic extends Mechanic {

    private boolean isEffect;

    /**
     * Default constructor for serializer
     */
    public LightningMechanic() {
    }

    public LightningMechanic(boolean isEffect) {
        this.isEffect = isEffect;
    }

    public boolean isEffect() {
        return isEffect;
    }

    @Override
    public String getKeyword() {
        return "Lightning";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/mechanics/lightning";
    }

    @Override
    protected void use0(CastData cast) {
        Location strikeLocation = cast.getTargetLocation();
        World world = cast.getTargetWorld();
        if (strikeLocation == null || world == null) {
            return;
        }

        if (isEffect)
            world.strikeLightningEffect(strikeLocation);
        else
            world.strikeLightning(strikeLocation);
    }

    @NotNull
    @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        boolean isEffect = data.of("Effect").getBool(false);

        return applyParentArgs(data, new LightningMechanic(isEffect));
    }
}