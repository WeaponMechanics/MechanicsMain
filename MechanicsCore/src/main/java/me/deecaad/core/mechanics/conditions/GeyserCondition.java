package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.geysermc.geyser.api.GeyserApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeyserCondition extends Condition {

    /**
     * Default constructor for serializer.
     */
    public GeyserCondition() {
    }

    @Override
    public boolean isAllowed0(CastData cast) {
        if (cast.getTarget() == null)
            return false;

        try {
            return GeyserApi.api().isBedrockPlayer(cast.getTarget().getUniqueId());
        } catch (Throwable ex) {
            MechanicsCore.debug.error("Tried to use GeyserCondition but GeyserMC is not installed!");
            return false;
        }
    }

    @Override
    public String getKeyword() {
        return "Geyser";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/integrations/geysermc#geyser-condition";
    }

    @NotNull
    @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new GeyserCondition());
    }
}
