package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.MechanicCaster;
import me.deecaad.core.mechanics.serialization.Argument;
import me.deecaad.core.mechanics.serialization.datatypes.DataType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.deecaad.core.MechanicsCore.debug;

public class EntitiesInRadiusTargeter extends Targeter<Entity> {

    private double radius;
    private double radiusSquared;
    private boolean isLiving;
    private EntityType only;

    public EntitiesInRadiusTargeter() {
        super("EntitiesInRadius",
                new Argument("radius", DataType.DOUBLE, "r"),
                new Argument("isLiving", DataType.BOOLEAN, "living"),
                new Argument("entity", DataType.ENTITY, "type")
        );
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
