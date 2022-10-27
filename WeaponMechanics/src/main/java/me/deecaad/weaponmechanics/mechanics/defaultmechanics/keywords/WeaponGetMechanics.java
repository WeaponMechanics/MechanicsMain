package me.deecaad.weaponmechanics.mechanics.defaultmechanics.keywords;

import me.deecaad.core.mechanics.IMechanic;
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

    public WeaponGetMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Weapon_Get_Mechanics";
    }
}