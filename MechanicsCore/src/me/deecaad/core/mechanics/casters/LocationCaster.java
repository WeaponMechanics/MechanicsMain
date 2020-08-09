package me.deecaad.core.mechanics.casters;

import org.bukkit.Location;

public class LocationCaster implements MechanicCaster {
    
    private Location loc;
    
    public LocationCaster(Location loc) {
        this.loc = loc;
    }
    
    @Override
    public Location getLocation() {
        return loc;
    }
    
    public void setLocation(Location loc) {
        this.loc = loc;
    }
}
