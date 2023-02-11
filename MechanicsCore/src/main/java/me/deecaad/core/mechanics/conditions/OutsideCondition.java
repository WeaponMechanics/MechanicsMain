package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.conditions.Condition;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class OutsideCondition extends Condition {

    public OutsideCondition() {}

    @Override
    protected boolean isAllowed0(CastData cast) {
        Location location = cast.getTargetLocation();
        return (location.getWorld().getHighestBlockYAt(location) <= location.getY() + 1.0D);
    }

    @Override
    public String getKeyword() {
        return "Outside";
    }

    @NotNull
    @Override
    public Condition serialize(SerializeData data) throws SerializerException {
        return applyParentArgs(data, new OutsideCondition());
    }
}