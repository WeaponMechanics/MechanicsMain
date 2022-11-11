package me.deecaad.weaponmechanics.mechanics.keywords;

import me.deecaad.core.mechanics.IMechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

/**
 * For ammo
 */
public class OutOfAmmoMechanics extends Mechanics {

    public OutOfAmmoMechanics() { }

    public OutOfAmmoMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Out_Of_Ammo";
    }
}