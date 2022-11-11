package me.deecaad.weaponmechanics.mechanics.keywords;

import me.deecaad.core.mechanics.IMechanic;
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

    public VictimMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Victim_Mechanics";
    }
}