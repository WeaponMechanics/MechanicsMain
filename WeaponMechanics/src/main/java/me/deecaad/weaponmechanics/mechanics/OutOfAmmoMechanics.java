package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

/**
 * For ammo
 */
public class OutOfAmmoMechanics extends Mechanics {

    public OutOfAmmoMechanics() {
    }

    public OutOfAmmoMechanics(List<Mechanic> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Out_Of_Ammo";
    }
}