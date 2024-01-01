package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GlidingCondition extends Condition {

    /**
     * Default constructor for serializer.
     */
    public GlidingCondition() {
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        LivingEntity target = cast.getTarget();
        return target != null && target.isGliding();
    }

    @Override
    public String getKeyword() {
        return "Gliding";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/conditions/gliding";
    }

    @NotNull
    @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new GlidingCondition());
    }
}
