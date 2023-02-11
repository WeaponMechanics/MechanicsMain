package me.vierdant.bridgedmechanics.tmep;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.conditions.Condition;
import org.jetbrains.annotations.NotNull;

public class OnGroundCondition extends Condition {

    public OnGroundCondition() {}

    @Override
    protected boolean isAllowed0(CastData cast) {
        if (cast.getTarget() == null) return false;
        return cast.getTarget().isOnGround();
    }

    @Override
    public String getKeyword() {
        return "OnGround";
    }

    @NotNull
    @Override
    public Condition serialize(SerializeData data) throws SerializerException {
        return applyParentArgs(data, new OnGroundCondition());
    }
}