package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.reload.ItemAmmo;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AmmoListeners implements Listener {

    @EventHandler
    public void click(InventoryClickEvent e) {
        Inventory clickedInventory = e.getClickedInventory();
        InventoryView view = e.getView();
        Inventory bottomInventory = view.getBottomInventory();
        if (!(e.getWhoClicked() instanceof Player) || e.isCancelled()
                || clickedInventory == null || !clickedInventory.equals(bottomInventory)) {
            // If cancelled, or clicked inventory doesn't equal player inventory
            return;
        }

        ItemStack magazineItem = e.getCurrentItem();
        if (magazineItem.getType() == Material.AIR) return;

        Integer magazineAmmoLeft = TagHelper.getIntegerTag(magazineItem, CustomTag.ITEM_AMMO_LEFT);

        // Check if clicked item is magazine item (so it can be filled)
        if (magazineAmmoLeft == null) return;

        String magazineName = TagHelper.getStringTag(magazineItem, CustomTag.ITEM_AMMO_NAME);
        if (magazineName == null) return; // this shouldn't even be possible, but just quick check

        ItemStack ammoItem = e.getCursor();
        if (ammoItem.getType() == Material.AIR) return;

        String ammoName = TagHelper.getStringTag(ammoItem, CustomTag.ITEM_AMMO_NAME);

        // Check that cursor item is ammo item
        // And that it isn't magazine item
        if (ammoName == null && TagHelper.getIntegerTag(ammoItem, CustomTag.ITEM_AMMO_LEFT) == null) return;

        // Check that ammo types are same
        if (!magazineName.equals(ammoName)) return;

        ItemAmmo itemAmmo = ItemAmmo.getByName(magazineName);
        if (itemAmmo == null) return; // This ammo has been removed...?

        int maximumMagazineSize = itemAmmo.getMaximumMagazineSize();
        if (magazineAmmoLeft >= maximumMagazineSize) return; // Already full

        int maximumFillAmount = maximumMagazineSize - magazineAmmoLeft;
        int availableAmount = ammoItem.getAmount();

        if (maximumFillAmount > availableAmount) {
            magazineItem = TagHelper.setIntegerTag(magazineItem, CustomTag.ITEM_AMMO_LEFT, magazineAmmoLeft + availableAmount);
            view.setCursor(null);
        } else {
            magazineItem = TagHelper.setIntegerTag(magazineItem, CustomTag.ITEM_AMMO_LEFT, magazineAmmoLeft + maximumFillAmount);

            ammoItem.setAmount(availableAmount - maximumFillAmount);
            view.setCursor(ammoItem);
        }

        Player player = (Player) e.getWhoClicked();

        ItemMeta magazineMeta = magazineItem.getItemMeta();
        magazineMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(magazineMeta.getDisplayName(), player, magazineItem, null));
        magazineMeta.setLore(PlaceholderAPI.applyPlaceholders(magazineMeta.getLore(), player, magazineItem, null));
        magazineItem.setItemMeta(magazineMeta);

        clickedInventory.setItem(e.getSlot(), magazineItem);

        e.setCancelled(true);
    }
}