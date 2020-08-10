package me.deecaad.core.__mechanics;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface IMechanic {

    /**
     * @return the task id if used (0 otherwise)
     */
    int use(Entity entity);

    /**
     * @return the task id if used (0 otherwise)
     */
    int use(Location location);
}
