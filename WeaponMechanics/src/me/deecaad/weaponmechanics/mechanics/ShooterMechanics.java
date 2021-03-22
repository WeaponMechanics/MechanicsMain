package me.deecaad.weaponmechanics.mechanics;

import java.util.List;

public class ShooterMechanics extends Mechanics {

    public ShooterMechanics() { }

    public ShooterMechanics(List<IMechanic<?>> mechanicList) {
        super(mechanicList);
    }

    @Override
    public String getKeyword() {
        return "Shooter_Mechanics";
    }
}