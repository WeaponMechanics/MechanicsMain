package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.MechanicCaster;
import me.deecaad.core.mechanics.serialization.Argument;
import me.deecaad.core.mechanics.serialization.StringSerializable;

import java.util.Collection;
import java.util.List;

public abstract class Targeter<T> extends StringSerializable<Targeter<T>> {
    
    protected Targeter(String name, Argument... args) {
        super(name, args);
    }
    
    protected Targeter(String name, Collection<? extends Argument> args) {
        super(name, args);
    }
    
    public abstract List<T> getTargets(MechanicCaster caster, List<T> list);
}
