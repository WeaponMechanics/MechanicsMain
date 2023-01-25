package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TargetTargeter extends Targeter {

    /**
     * Default constructor for serializer.
     */
    public TargetTargeter() {
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    protected List<CastData> getTargets0(CastData cast) {
        return List.of(cast.clone());
    }

    @Override
    public String getKeyword() {
        return "Target";
    }

    @Nullable
    @Override
    public String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/TargetTargeter";
    }

    @NotNull
    @Override
    public Targeter serialize(SerializeData data) throws SerializerException {
        return applyParentArgs(data, new TargetTargeter());
    }
}
