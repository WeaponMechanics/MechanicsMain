package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

/**
 * Targets the nearest entities
 */
@SerializerData(name = "@nearest", args = {"amount~INTEGER", "distanceToCheck~DOUBLE~distance"})
public class NearestTargeter extends EntityTargeter {

    private double distance;
    private int amount;

    /**
     * Default constructor for serializer
     */
    public NearestTargeter() {
    }

    @Nullable
    @Override
    public List<Entity> getTargets(MechanicCaster caster) {
        List<Entity> temp = new ArrayList<>(amount);

        Location origin = caster.getLocation();
        List<Entity> entities = new ArrayList<>(origin.getWorld()
                .getNearbyEntities(caster.getLocation(), distance, distance, distance, predicate));

        // Sort the entities by distance to origin so we can just grab
        // the entities from the front of the list
        entities.sort(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(origin)));
        for (int i = 0; i < amount; i++) {
            temp.add(entities.get(i));
        }

        return temp;
    }

    @Override
    public Targeter<Entity> serialize(Map<String, Object> data) {

        distance = (double) data.getOrDefault("distanceToCheck", 100);
        amount = (int) data.getOrDefault("amount", 1);

        if (distance <= 0) {
            debug.error("@nearest distance should be greater than 0");
        }
        if (amount <= 0) {
            debug.error("@nearest amount should be greater than 0!");
        }

        return super.serialize(data);
    }
}
