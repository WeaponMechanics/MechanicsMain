package me.deecaad.weaponmechanics.mechanics.defaultmechanics.keywords;

import me.deecaad.core.mechanics.IMechanic;
import me.deecaad.core.mechanics.Mechanics;

import java.util.List;

/**
 * For equipping
 */
public class EquipMechanics extends Mechanics {

    /**
     * Default constructor for serializer
     */
    public EquipMechanics() {
    }

    public EquipMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Weapon_Equip_Mechanics";
    }
}