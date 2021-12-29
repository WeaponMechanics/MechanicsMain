package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.inventory.ItemStack;

public interface IAmmoType {

    String getAmmoName();

    String getSymbol();

    boolean hasAmmo(IPlayerWrapper playerWrapper);

    int removeAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount);

    void giveAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount);

    int getMaximumAmmo(IPlayerWrapper playerWrapper);
}