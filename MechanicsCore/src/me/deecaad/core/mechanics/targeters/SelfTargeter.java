package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.MechanicCaster;
import me.deecaad.core.mechanics.serialization.Argument;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public class SelfTargeter extends Targeter<Location> {
    
    public SelfTargeter() {
        super("self");
    }
    
    @Override
    public List<Location> getTargets(MechanicCaster caster, List<Location> list) {
        list.clear();
        
        list.add(caster.getLocation());
        return list;
    }
    
    @Override
    public Targeter<Location> serialize(Map<String, Object> data) {
        return this;
    }
}
