package me.deecaad.weaponmechanics.weapon.explode.exposures;

import me.deecaad.weaponmechanics.weapon.explode.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionShape;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanceExposure implements ExplosionExposure {

    @Nonnull
    @Override
    public Map<LivingEntity, Double> mapExposures(@Nonnull Location origin, @Nonnull ExplosionShape shape) {

        List<LivingEntity> entities = shape.getEntities(origin);
        Map<LivingEntity, Double> temp = new HashMap<>(entities.size());

        // The outer "shell" of the explosion
        double maxDistance = shape.getMaxDistance();

        for (LivingEntity entity : entities) {

            // Determine how far away the entity is from the explosion
            Vector vector = origin.toVector().subtract(entity.getLocation().toVector());
            double length = vector.length();

            double distanceRate = (maxDistance - length) / maxDistance;
            temp.put(entity, distanceRate);
        }

        return temp;
    }
}
