package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;

public interface IAmmoType {

    String getAmmoName();

    boolean hasAmmo(IPlayerWrapper playerWrapper);

    int removeAmmo(IPlayerWrapper playerWrapper, int amount);

    void giveAmmo(IPlayerWrapper playerWrapper, int amount);

    int getMaximumAmmo(IPlayerWrapper playerWrapper);
}