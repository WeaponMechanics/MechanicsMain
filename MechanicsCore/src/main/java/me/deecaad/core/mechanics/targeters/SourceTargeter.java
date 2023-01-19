package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.mechanics.CastData;

import java.util.List;
import java.util.Map;

public class SourceTargeter extends Targeter {

    /**
     * Default constructor for serializer.
     */
    public SourceTargeter() {
    }

    public SourceTargeter(Map<Argument, Object> args) throws InlineException {
        super(args);
    }

    @Override
    public ArgumentMap args() {
        return super.args();
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
}
