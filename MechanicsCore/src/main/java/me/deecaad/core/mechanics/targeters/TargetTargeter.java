package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.mechanics.CastData;

import java.util.List;
import java.util.Map;

public class TargetTargeter extends Targeter {

    /**
     * Default constructor for serializer.
     */
    public TargetTargeter() {
    }

    public TargetTargeter(Map<Argument, Object> args) throws InlineException {
        super(args);
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
}
