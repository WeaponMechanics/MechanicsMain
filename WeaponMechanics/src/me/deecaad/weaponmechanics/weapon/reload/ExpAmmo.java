package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;

public class ExpAmmo implements IAmmoType {

    private int expCost;

    public ExpAmmo(int expCost) {
        this.expCost = expCost;
    }

    @Override
    public int getAmount(IEntityWrapper entityWrapper) {
        return 0;
    }

    @Override
    public int remove(IEntityWrapper entityWrapper, int amount) {
        return 0;
    }

    @Override
    public void give(IEntityWrapper entityWrapper, int amount) {

    }
}
