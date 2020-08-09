package me.deecaad.core.mechanics.types;

import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.casters.EntityCaster;
import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

@SerializerData(name = "damage", args = "amount~DOUBLE")
public class DamageMechanic extends Mechanic {
    
    private double amount;
    
    /**
     * Default constructor for serializer
     */
    public DamageMechanic() {
    }
    
    @Override
    public Mechanic serialize(Map<String, Object> data) {
        amount = (double) data.getOrDefault("amount", 0.0);
        return this;
    }
    
    @Override
    public void cast(MechanicCaster caster, Location target) {
        // Do nothing...
    }
    
    @Override
    public void cast(MechanicCaster caster, Entity target) {
        if (target.getType().isAlive()) {
            LivingEntity toDamage = (LivingEntity) target;
            Entity entity = caster instanceof EntityCaster ? ((EntityCaster) caster).getEntity() : null;
            
            toDamage.damage(amount, entity);
        }
    }
    
    @Override
    public void cast(MechanicCaster caster, Player target) {
        Entity entity = caster instanceof EntityCaster ? ((EntityCaster) caster).getEntity() : null;
        target.damage(amount, entity);
    }
}
