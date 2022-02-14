package me.deecaad.weaponmechanics.mechanics.defaultmechanics.keywords;

import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;

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