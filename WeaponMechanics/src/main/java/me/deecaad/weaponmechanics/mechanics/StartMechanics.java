package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

/**
 * For reload
 */
public class StartMechanics extends Mechanics {

    /**
     * Default constructor for serializer
     */
    public StartMechanics() {
    }

    public StartMechanics(List<Mechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Start_Mechanics";
    }
}