package me.deecaad.weaponmechanics.mechanics.keywords;

import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

/**
 * For damaging
 */
public class VictimMechanics extends Mechanics {

    /**
     * Default constructor for serializer
     */
    public VictimMechanics() {
    }

    public VictimMechanics(List<Mechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Victim_Mechanics";
    }
}