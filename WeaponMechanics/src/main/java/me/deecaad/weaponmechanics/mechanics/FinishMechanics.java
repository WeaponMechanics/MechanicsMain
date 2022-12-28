package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

/**
 * For reload
 */
public class FinishMechanics extends Mechanics {

    /**
     * Default constructor for serializer
     */
    public FinishMechanics() {
    }

    public FinishMechanics(List<Mechanic> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Finish_Mechanics";
    }
}