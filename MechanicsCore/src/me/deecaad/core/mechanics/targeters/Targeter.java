package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.StringSerializable;

import java.util.List;

public interface Targeter<T> extends StringSerializable<Targeter<T>> {
    
    List<T> getTargets(MechanicCaster caster, List<T> list);
}
