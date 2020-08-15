package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.StringSerializable;

import javax.annotation.Nullable;
import java.util.List;

public interface Targeter<T> extends StringSerializable<Targeter<T>> {

    @Nullable
    List<T> getTargets(MechanicCaster caster);
}
