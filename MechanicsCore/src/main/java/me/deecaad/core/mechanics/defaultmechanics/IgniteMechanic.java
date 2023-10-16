package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IgniteMechanic extends Mechanic {

    private int ticks;

    /**
     * Default constructor for serializer
     */
    public IgniteMechanic() {
    }

    public IgniteMechanic(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public String getKeyword() {
        return "Ignite";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/mechanics/ignite";
    }

    @Override
    protected void use0(CastData cast) {

        // We must have an entity to ignite
        if (cast.getTarget() == null)
            return;

        cast.getTarget().setFireTicks(ticks);
    }

    @NotNull
    @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        int ticks = data.of("Time").getInt(100);

        return applyParentArgs(data, new IgniteMechanic(ticks));
    }
}
