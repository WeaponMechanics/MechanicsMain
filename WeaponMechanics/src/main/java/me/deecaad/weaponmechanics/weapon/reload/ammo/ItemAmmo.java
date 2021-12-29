package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.placeholder.PlaceholderAPI;
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
    public int removeAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        if (amount == 0) return 0;

        if (magazine != null) {

            // todo

            return 0;
        }

        // todo

        return 0;
    }

    @Override
    public void giveAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        if (amount == 0) return;

        if (magazine != null) {
            ItemStack cloneMagazine = magazine.clone();

            // todo

            return;
        }

        // todo
    }

    @Override
    public int getMaximumAmmo(IPlayerWrapper playerWrapper, int maximumMagazineSize) {

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
