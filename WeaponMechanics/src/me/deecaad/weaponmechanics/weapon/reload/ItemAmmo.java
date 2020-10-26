package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;

        // todo

        return 0;
    }

    @Override
    public int remove(IEntityWrapper entityWrapper, int amount) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;

        return 0;
    }

    @Override
    public void give(IEntityWrapper entityWrapper, int amount) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return;

    }

    private boolean isMatch(ItemStack ammoStack, ItemStack other) {
        double version = CompatibilityAPI.getVersion();
        if (ammoStack.getType() != other.getType()) {
            return false;
        }
        if (version < 1.13 && ammoStack.getData().getData() != other.getData().getData()) {
            return false;
        }
        ItemMeta ammoMeta = ammoStack.getItemMeta();
        ItemMeta otherMeta = other.getItemMeta();
        if (ammoMeta.hasDisplayName() && !otherMeta.hasDisplayName()
                || !ammoMeta.getDisplayName().equalsIgnoreCase(otherMeta.getDisplayName())) {
            // If weapon would have display name, but other doesn't
            // OR
            // If weapon and other display names doesn't match
            return false;
        }
        if (ammoMeta.hasLore() && !otherMeta.hasLore()
                || !ammoMeta.getLore().equals(otherMeta.getLore())) {
            // If weapon would have lore, but other doesn't
            // OR
            // If weapon and other lore doesn't match
            return false;
        }
        return true;
    }
}