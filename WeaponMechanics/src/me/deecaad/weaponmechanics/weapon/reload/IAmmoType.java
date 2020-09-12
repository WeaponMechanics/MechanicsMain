package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;

public interface IAmmoType {

    /**
     * @param entityWrapper the entity
     * @return the amount of this type ammo entity currently has
     */
    int getAmount(IEntityWrapper entityWrapper);

    /**
     * @param entityWrapper the entity
     * @param amount the amount to remove ammo
     * @return the amount of ammo that was removed from entity
     */
    int remove(IEntityWrapper entityWrapper, int amount);

    /**
     * @param entityWrapper the entity
     * @param amount the amount of ammo to give for entity
     */
    void give(IEntityWrapper entityWrapper, int amount);
}