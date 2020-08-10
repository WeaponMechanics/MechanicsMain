package me.deecaad.core.mechanics;

import me.deecaad.core.mechanics.casters.MechanicCaster;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface Targetable {

    void cast(MechanicCaster caster, Location target);
    
    void cast(MechanicCaster caster, Entity target);
    
    void cast(MechanicCaster caster, Player target);
}
