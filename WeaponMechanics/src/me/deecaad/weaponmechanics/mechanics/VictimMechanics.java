package me.deecaad.weaponmechanics.mechanics;

import java.util.List;

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