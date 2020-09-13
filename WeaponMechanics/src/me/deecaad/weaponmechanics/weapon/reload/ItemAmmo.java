package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class ItemAmmo implements IAmmoType {

    private ItemStack magazine;
    private ItemStack ammo;

    public ItemAmmo(@Nullable ItemStack magazine, ItemStack ammo) {
        this.magazine = magazine;
        this.ammo = ammo;
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