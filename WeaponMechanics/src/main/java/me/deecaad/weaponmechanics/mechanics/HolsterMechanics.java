package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

public class HolsterMechanics extends Mechanics {

    /**
     * Default constructor for serializer
     */
    public HolsterMechanics() {
    }

    public HolsterMechanics(List<Mechanic> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Weapon_Holster_Mechanics";
    }
}