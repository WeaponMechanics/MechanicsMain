package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.casters.EntityCaster;
import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;

import java.util.List;
import java.util.Map;

@SerializerData(name = "self")
public class SelfTargeter implements Targeter<Object> {

    /**
     * Default constructor for serializer
     */
    public SelfTargeter() {
    }
    
    @Override
    public List<Object> getTargets(MechanicCaster caster, List<Object> list) {
        list.clear();
        
        list.add(caster instanceof EntityCaster ? ((EntityCaster) caster).getEntity() : caster.getLocation());
        return list;
    }
    
    @Override
    public Targeter<Object> serialize(Map<String, Object> data) {

        // Nothing needs to be done to the serializer
        return this;
    }
}
