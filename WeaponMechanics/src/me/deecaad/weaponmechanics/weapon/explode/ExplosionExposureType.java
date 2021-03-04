package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.weaponmechanics.weapon.explode.exposures.DefaultExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DistanceExposure;

public enum ExplosionExposureType {

    /**
     * Entities within the explosion's area of effect always have
     * 100% exposure, regardless of obstacles and positioning
     */
    NONE,

    /**
     * Damage is only based on the distance between the <code>LivingEntity</code>
     * involved and the origin of the explosion
     * @see DistanceExposure
     */
    DISTANCE,

    /**
     * Damage uses minecraft's explosion exposure method
     * @see DefaultExposure
     */
    DEFAULT
}
