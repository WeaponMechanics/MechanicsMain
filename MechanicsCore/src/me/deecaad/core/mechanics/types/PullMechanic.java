package me.deecaad.core.mechanics.types;

import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import me.deecaad.core.utils.VectorUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;

@SerializerData(name = "pull", args = "velocity~DOUBLE~amount")
public class PullMechanic extends Mechanic {

    // This is the set length of the vector
    private double velocity;

    /**
     * Default constructor for serializer
     */
    public PullMechanic() {
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    @Override
    public Mechanic serialize(Map<String, Object> data) {
        velocity = (double) data.getOrDefault("velocity", -1);
        return super.serialize(data);
    }

    @Override
    public void cast(MechanicCaster caster, Location target) {
        // Do nothing...
    }

    @Override
    public void cast(MechanicCaster caster, Entity target) {
        Vector between = caster.getLocation().subtract(target.getLocation()).toVector();
        if (velocity != -1) VectorUtils.setLength(between, velocity);
        target.setVelocity(between);
    }

    @Override
    public void cast(MechanicCaster caster, Player target) {
        Vector between = caster.getLocation().subtract(target.getLocation()).toVector();
        if (velocity != -1) VectorUtils.setLength(between, velocity);
        target.setVelocity(between);
    }
}
