package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;

import java.util.List;

/**
 * For damaging
 */
public class VictimMechanics extends Mechanics {

    public VictimMechanics() { }

    public VictimMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Victim_Mechanics";
    }
}