package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class SourceTargeter extends Targeter {

    /**
     * Default constructor for serializer.
     */
    public SourceTargeter() {
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    public Iterator<CastData> getTargets0(CastData cast) {
        cast.setTargetEntity(cast.getSource());
        return new SingleIterator<>(cast);
    }

    @Override
    public String getKeyword() {
        return "Source";
    }

    @Nullable
    @Override
    public String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/SourceTargeter";
    }

    @NotNull
    @Override
    public Targeter serialize(@NotNull SerializeData data) throws SerializerException {
        return applyParentArgs(data, new SourceTargeter());
    }
}
