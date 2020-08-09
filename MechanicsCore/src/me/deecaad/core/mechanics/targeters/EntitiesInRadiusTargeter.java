package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.deecaad.core.MechanicsCore.debug;

@SerializerData(name = "@entitiesInRadius", args = {"radius~DOUBLE~r", "isLiving~BOOLEAN~living", "entity~ENTITY~type"})
public class EntitiesInRadiusTargeter implements Targeter<Entity> {

    private double radius;
    private double radiusSquared;
    private boolean isLiving;
    private EntityType only;

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

    public boolean isLiving() {
        return isLiving;
    }

    public void setLiving(boolean living) {
        isLiving = living;
    }

    public EntityType getOnly() {
        return only;
    }

    public void setOnly(@Nullable EntityType only) {
        this.only = only;
    }

    @Override
    public List<Entity> getTargets(MechanicCaster caster, List<Entity> list) {
        Location loc = caster.getLocation();

        return loc.getWorld().getNearbyEntities(loc, radius, radius, radius, entity ->
            (isLiving && entity.getType().isAlive()) || (only != null && only == entity.getType())).stream()
                .filter(entity -> entity.getLocation().distanceSquared(loc) <= radiusSquared)
                .collect(Collectors.toList());
    }

    @Override
    public Targeter<Entity> serialize(Map<String, Object> data) {

        double radius = (double) data.get("radius");
        boolean isLiving = (boolean) data.get("isLiving");
        EntityType only = (EntityType) data.get("entity");

        debug.validate(radius > 0, "Radius must be positive!");

        setRadius(radius);
        setLiving(isLiving);
        setOnly(only);

        return this;
    }
}
