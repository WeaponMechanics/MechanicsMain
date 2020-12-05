package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;

public interface IAmmoType {

    /**
     * @param entityWrapper the entity
     * @return whether entity has at least 1 ammo
     */
    boolean hasAmmo(IEntityWrapper entityWrapper);

    /**
     * @param entityWrapper the entity
     * @param magazineSize  the weapon's full magazine size
     * @return the amount of this type ammo entity currently has
     */
    int getAmount(IEntityWrapper entityWrapper, int magazineSize);

    /**
     * @param entityWrapper the entity
     * @param amount        the amount to remove ammo
     * @param magazineSize  the weapon's full magazine size
     * @return the amount of ammo that was removed from entity
     */
    int remove(IEntityWrapper entityWrapper, int amount, int magazineSize);

    /**
     * @param entityWrapper the entity
     * @param amount        the amount of ammo to give for entity
     * @param magazineSize  the weapon's full magazine size
     */
    void give(IEntityWrapper entityWrapper, int amount, int magazineSize);
}