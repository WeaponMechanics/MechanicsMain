package me.deecaad.weaponmechanics.mechanics.defaultmechanics.keywords;

import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;

import java.util.List;

public class HolsterMechanics extends Mechanics {

    public HolsterMechanics() { }

    public HolsterMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Weapon_Holster_Mechanics";
    }
}