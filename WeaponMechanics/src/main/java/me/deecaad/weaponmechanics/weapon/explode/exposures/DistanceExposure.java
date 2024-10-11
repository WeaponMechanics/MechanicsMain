package me.deecaad.weaponmechanics.weapon.explode.exposures;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ExplosionShape;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DistanceExposure implements ExplosionExposure {

    @NotNull @Override
    public Object2DoubleMap<LivingEntity> mapExposures(@NotNull Location origin, @NotNull ExplosionShape shape) {

        List<LivingEntity> entities = shape.getEntities(origin);
        Object2DoubleMap<LivingEntity> temp = new Object2DoubleOpenHashMap<>(entities.size());

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
