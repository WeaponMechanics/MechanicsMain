package me.deecaad.weaponmechanics.mechanics.defaultmechanics.keywords;

import me.deecaad.core.mechanics.IMechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

/**
 * For damaging
 */
public class ShooterMechanics extends Mechanics {

    /**
     * Default constructor for serializer
     */
    public ShooterMechanics() {
    }

    public ShooterMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Shooter_Mechanics";
    }
}