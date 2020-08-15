package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.deecaad.core.MechanicsCore.debug;

@SerializerData(name = "@entitiesInRadius", args = "radius~DOUBLE~r")
public class EntitiesInRadiusTargeter extends EntityTargeter {

    private double radius;
    private double radiusSquared;

    /**
     * Default constructor for serializer
     */
    public EntitiesInRadiusTargeter() {
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        if (radius < 0) throw new IllegalArgumentException("Expected positive number for radius, got: " + radius);

        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    @Override
    public List<Entity> getTargets(MechanicCaster caster) {
        Location loc = caster.getLocation();
        boolean hasNoFilter = only == null && !living;

        return loc.getWorld().getNearbyEntities(loc, radius, radius, radius, entity ->
            hasNoFilter || (living && entity.getType().isAlive()) || (only != null && only == entity.getType())).stream()
                .filter(entity -> entity.getLocation().distanceSquared(loc) <= radiusSquared)
                .collect(Collectors.toList());
    }

    @Override
    public Targeter<Entity> serialize(Map<String, Object> data) {

        double radius = (double) data.getOrDefault("radius", 1.0);
        debug.validate(radius > 0, "Radius must be positive!");
        setRadius(radius);

        return super.serialize(data);
    }
}
