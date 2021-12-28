package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;

public class ExperienceAmmo implements IAmmoType {

    // Defined in ammo types list
    private String ammoName;

    private int experienceAsAmmoCost;

    @Override
    public String getAmmoName() {
        return null;
    }

    @Override
    public boolean hasAmmo(IPlayerWrapper playerWrapper) {
        return false;
    }

    @Override
    public int removeAmmo(IPlayerWrapper playerWrapper, int amount) {
        return 0;
    }

    @Override
    public void giveAmmo(IPlayerWrapper playerWrapper, int amount) {

    }

    @Override
    public int getMaximumAmmo(IPlayerWrapper playerWrapper) {
        return 0;
    }
}
