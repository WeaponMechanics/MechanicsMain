package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.inline.types.DoubleType;
import me.deecaad.core.mechanics.CastData;

import java.util.Map;

public class RangeCondition extends Condition {

    public static final Argument MIN = new Argument("min", new DoubleType(0.0), 0.0);
    public static final Argument MAX = new Argument("max", new DoubleType(0.0), -1.0);

    private final double minSquared;
    private final double maxSquared;

    public RangeCondition(Map<Argument, Object> args) throws InlineException {
        double min = (double) args.get(MIN);
        double max = (double) args.get(MAX);

        if (min == 0.0 && max == -1.0)
            throw new InlineException(getInlineKeyword(), new SerializerException("", new String[] {"Found an empty RangeCondition, make sure you use either 'min' or 'max'"}, ""));

        minSquared = min * min;
        maxSquared = max == -1.0 ? -1.0 : max * max;
    }

    @Override
    public ArgumentMap args() {
        return new ArgumentMap(MIN, MAX);
    }

    @Override
    public String getKeyword() {
        return "Range";
    }

    @Override
    public boolean isAllowed(CastData cast) {
        double distanceSquared = cast.getTargetLocation().distanceSquared(cast.getSourceLocation());

        if (distanceSquared < minSquared)
            return false;
        if (maxSquared != -1.0 && distanceSquared > maxSquared)
            return false;

        return true;
    }
}
