package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

/**
 * For equipping
 */
public class WeaponGetMechanics extends Mechanics {

    /**
     * Default constructor for serializer
     */
    public WeaponGetMechanics() {
    }

    public WeaponGetMechanics(List<Mechanic> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Weapon_Get_Mechanics";
    }
}