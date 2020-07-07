package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.weaponmechanics.weapon.explode.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.explode.ExplosionShape;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoidExposure implements ExplosionExposure {

    @Nonnull
    @Override
    public Map<LivingEntity, Double> mapExposures(@Nonnull Location origin, @Nonnull ExplosionShape shape) {

        List<LivingEntity> entities = shape.getEntities(origin);
        Map<LivingEntity, Double> exposures = new HashMap<>(entities.size());

        for (LivingEntity entity : entities) {
            exposures.put(entity, 1.0);
        }

        return exposures;
    }
}
