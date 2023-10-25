package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

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
    protected Iterator<CastData> getTargets0(CastData cast) {

        // Seems redundant, but actually SUPER important. Remember that the 'cast'
        // variable is reused by all mechanics. So we need to update the target.
        if (cast.getTarget() != null)
            cast.setTargetEntity(cast.getTarget());
        if (cast.hasTargetLocation())
            cast.setTargetLocation(cast.getTargetLocationSupplier());

        return new SingleIterator<>(cast);
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
    public Targeter serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new TargetTargeter());
    }
}
