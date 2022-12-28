package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.mechanics.Mechanic;
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

    public ShooterMechanics(List<Mechanic> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Shooter_Mechanics";
    }
}