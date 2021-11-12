package me.deecaad.weaponmechanics.weapon.explode.exposures;

import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ExplosionShape;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class VoidExposure implements ExplosionExposure {

    @Nonnull
    @Override
    public DoubleMap<LivingEntity> mapExposures(@Nonnull Location origin, @Nonnull ExplosionShape shape) {

        List<LivingEntity> entities = shape.getEntities(origin);
        DoubleMap<LivingEntity> exposures = new DoubleMap<>(entities.size());

        for (LivingEntity entity : entities) {
            exposures.put(entity, 1.0);
        }

        return exposures;
    }
}
