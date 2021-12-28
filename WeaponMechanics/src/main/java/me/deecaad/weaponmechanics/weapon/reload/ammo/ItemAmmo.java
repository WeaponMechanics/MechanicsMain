package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.inventory.ItemStack;

public class ItemAmmo implements IAmmoType {

    // Defined in ammo types list
    private String ammoName;

    private ItemStack ammo;
    private ItemStack magazine;
    private int maximumMagazineSize;
    private Mechanics notSameAmmoName;
    private Mechanics magazineAlreadyFull;
    private Mechanics magazineFilled;
    private AmmoConverter ammoConverter;

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
