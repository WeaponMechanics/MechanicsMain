package me.deecaad.weaponmechanics.weapon.explode;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Map;

public interface ExplosionExposure {

    /**
     * This method should return a list of entities that
     * are within this <code>Explosion</code> triggered
     * at the given <code>Location</code>.
     *
     * Conditions (Like player team, the cause of the
     * explosion, etc) are not used to filter entities,
     * that is handled separately
     *
     * The <code>Double</code> generic represents how much
     * "impact" the player gets. This should be a number (0, 1]
     * Higher numbers mean more damage and knockback
     *
     * @param origin
     * @param shape
     * @return The effected players and their impact level
     */
    @Nonnull
    Map<LivingEntity, Double> mapExposures(@Nonnull Location origin, @Nonnull ExplosionShape shape);
}
