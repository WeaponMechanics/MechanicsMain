package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.mechanics.CastData;

import java.util.List;
import java.util.Map;

public class SourceTargeter extends Targeter {

    public SourceTargeter(Map<Argument, Object> args) {
    }

    @Override
    public ArgumentMap args() {
        return new ArgumentMap();
    }

    @Override
    public String getKeyword() {
        return "Source";
    }

    @Override
    public List<CastData> getTargets(CastData cast) {
        CastData copy = cast.clone();
        copy.setTargetLocation(copy.getSourceLocation());
        copy.setTargetEntity(copy.getSource());
        return List.of(copy);
    }
}
