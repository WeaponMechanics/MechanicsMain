package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;

public abstract class RelativeTargeter extends Targeter {

    protected boolean isUseTarget;

    /**
     * Default constructor for serializer
     */
    public RelativeTargeter() {
    }

    @Override
    protected Targeter applyParentArgs(SerializeData data, Targeter targeter) throws SerializerException {
        RelativeTargeter relativeTargeter = (RelativeTargeter) super.applyParentArgs(data, targeter);
        relativeTargeter.isUseTarget = data.of("Use_Target").getBool(false);
        return relativeTargeter;
    }
}
