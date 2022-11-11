package me.deecaad.weaponmechanics.mechanics.keywords;

import me.deecaad.core.mechanics.IMechanic;
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

    public FinishMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Finish_Mechanics";
    }
}