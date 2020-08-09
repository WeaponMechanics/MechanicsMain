package me.deecaad.core.mechanics.casters;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public interface EntityCaster extends MechanicCaster {
    
    Entity getEntity();
    
    @Override
    default Location getLocation() {
        Entity entity = getEntity();
        
        return entity.getType().isAlive() ? ((LivingEntity) entity).getEyeLocation() : entity.getLocation();
    }
}
