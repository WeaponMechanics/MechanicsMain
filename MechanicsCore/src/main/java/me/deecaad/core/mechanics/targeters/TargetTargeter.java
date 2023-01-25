package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TargetTargeter extends Targeter {

    /**
     * Default constructor for serializer.
     */
    public TargetTargeter() {
    }

    @Override
    public String getKeyword() {
        return "Target";
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    protected List<CastData> getTargets0(CastData cast) {
        return List.of(cast.clone());
    }

    @NotNull
    @Override
    public Targeter serialize(SerializeData data) throws SerializerException {
        return applyParentArgs(data, new TargetTargeter());
    }
}
