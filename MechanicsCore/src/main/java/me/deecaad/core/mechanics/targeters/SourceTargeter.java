package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SourceTargeter extends Targeter {

    /**
     * Default constructor for serializer.
     */
    public SourceTargeter() {
    }

    @Override
    public String getKeyword() {
        return "Source";
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    public List<CastData> getTargets0(CastData cast) {
        CastData copy = cast.clone();
        copy.setTargetEntity(copy.getSource());
        if (copy.hasSourceLocation())
            copy.setTargetLocation(copy.getSourceLocation());
        return List.of(copy);
    }

    @NotNull
    @Override
    public Targeter serialize(SerializeData data) throws SerializerException {
        return applyParentArgs(data, new SourceTargeter());
    }
}
