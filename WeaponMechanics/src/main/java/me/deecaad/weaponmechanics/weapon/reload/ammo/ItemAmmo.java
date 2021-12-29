package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemAmmo implements IAmmoType {

    // Defined in ammo types list
    private String ammoName;

    private String symbol;
    private ItemStack ammo;
    private ItemStack magazine;
    private int maximumMagazineSize;
    private Mechanics notSameAmmoName;
    private Mechanics magazineAlreadyFull;
    private Mechanics magazineFilled;
    private AmmoConverter ammoConverter;

    @Override
    public String getAmmoName() {
        return ammoName;
    }

    @Override
    public String getSymbol() {
        return symbol != null ? symbol : ammoName;
    }

    @Override
    public boolean hasAmmo(IPlayerWrapper playerWrapper) {

        // todo

        return false;
    }

    @Override
    public int removeAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount) {
        if (magazine != null) {

            // todo

            // If removed amount > 0 meaning that magazine has been put back ->
            // Set HAS_MAGAZINE back to 1 to indicate that the magazine is now there.
            // When using magazines Unload_Ammo_On_Reload should always be true
            // so HAS_MAGAZINE should always be 0 here since giveAmmo() is called before this.
            CustomTag.HAS_MAGAZINE.setInteger(weaponStack, 1);

            return 0; // todo -> removed amount
        }

        // todo

        return 0;
    }

    @Override
    public void giveAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount) {
        Player player = playerWrapper.getPlayer();

        if (magazine != null) {
            ItemStack cloneMagazine = magazine.clone();

            if (amount == 0) {
                // Give empty magazine back if isn't yet been given
                if (CustomTag.HAS_MAGAZINE.getInteger(weaponStack) == 1) {
                    // 1 in HAS_MAGAZINE means that weapon still has magazine attached

                    updatePlaceholders(cloneMagazine, player);

                    // No need to set MAGAZINE_AMMO_LEFT to 0 since by default
                    // it's always 0 when cloning magazine

                    giveOrDrop(player, cloneMagazine);

                    // Set HAS_MAGAZINE to 0 to indicate that weapon doesn't even have empty
                    // magazine attached to it anymore
                    CustomTag.HAS_MAGAZINE.setInteger(weaponStack, 0);
                }
                return;
            }

            // todo

            return;
        }

        // When not using magazines there isn't need to give empty magazine back...
        if (amount == 0) return;

        // todo
    }

    @Override
    public int getMaximumAmmo(IPlayerWrapper playerWrapper) {

        // todo

        return 0;
    }

    private void updatePlaceholders(ItemStack itemStack, Player player) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;

        itemMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(itemMeta.getDisplayName(), player, itemStack, null));
        itemMeta.setLore(PlaceholderAPI.applyPlaceholders(itemMeta.getLore(), player, itemStack, null));
        itemStack.setItemMeta(itemMeta);
    }

    private void giveOrDrop(Player player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();

        // Check if inventory doesn't have any free slots
        if (inventory.firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation().add(0.0, 1.0, 0.0), itemStack);
            return;
        }
        inventory.addItem(itemStack);
    }
}
