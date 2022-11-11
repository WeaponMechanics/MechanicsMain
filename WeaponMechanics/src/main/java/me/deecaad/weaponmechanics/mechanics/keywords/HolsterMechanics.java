package me.deecaad.weaponmechanics.mechanics.keywords;

import me.deecaad.core.mechanics.IMechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

public class HolsterMechanics extends Mechanics {

    /**
     * Default constructor for serializer
     */
    public HolsterMechanics() {
    }

    public HolsterMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Weapon_Holster_Mechanics";
    }
}