package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.reload.ItemAmmo;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
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

        int magazineAmmoLeft = CustomTag.ITEM_AMMO_LEFT.getInteger(magazineItem);

        // Check if clicked item is magazine item (so it can be filled)
        //if (magazineAmmoLeft == null) return;

        String magazineName = CustomTag.ITEM_AMMO_NAME.getString(magazineItem);
        if (magazineName == null) return; // this shouldn't even be possible, but just quick check

        ItemStack ammoItem = e.getCursor();
        if (ammoItem.getType() == Material.AIR) return;

        String ammoName = CustomTag.ITEM_AMMO_NAME.getString(ammoItem);

        // Check that cursor item is ammo item
        // And that it isn't magazine item
        if (ammoName == null && CustomTag.ITEM_AMMO_LEFT.hasInteger(ammoItem)) return;


        ItemAmmo itemAmmo = ItemAmmo.getByName(magazineName);
        if (itemAmmo == null) {
            // This ammo has been removed...?
            WeaponMechanics.debug.log(LogLevel.DEBUG, "Magazine is probably removed with name of " + magazineName + ".");
            return;
        }
        Player player = (Player) e.getWhoClicked();
        IPlayerWrapper playerWrapper = WeaponMechanics.getPlayerWrapper(player);

        // Check that ammo types are same
        if (!magazineName.equals(ammoName)) {
            itemAmmo.useNotSameAmmoName(new CastData(playerWrapper));
            e.setCancelled(true);
            return;
        }

        int maximumMagazineSize = itemAmmo.getMaximumMagazineSize();
        if (magazineAmmoLeft >= maximumMagazineSize) {
            // Already full
            itemAmmo.useMagazineAlreadyFull(new CastData(playerWrapper));
            e.setCancelled(true);
            return;
        }

        int maximumFillAmount = maximumMagazineSize - magazineAmmoLeft;
        int availableAmount = ammoItem.getAmount();

        if (maximumFillAmount > availableAmount) {
            CustomTag.ITEM_AMMO_LEFT.setInteger(magazineItem, magazineAmmoLeft + availableAmount);
            view.setCursor(null);
        } else {
            CustomTag.ITEM_AMMO_LEFT.setInteger(magazineItem, magazineAmmoLeft + maximumFillAmount);

            ammoItem.setAmount(availableAmount - maximumFillAmount);
            view.setCursor(ammoItem);
        }

        ItemMeta magazineMeta = magazineItem.getItemMeta();
        magazineMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(magazineMeta.getDisplayName(), player, magazineItem, null));
        magazineMeta.setLore(PlaceholderAPI.applyPlaceholders(magazineMeta.getLore(), player, magazineItem, null));
        magazineItem.setItemMeta(magazineMeta);

        clickedInventory.setItem(e.getSlot(), magazineItem);

        // success
        itemAmmo.useMagazineFilled(new CastData(playerWrapper));

        e.setCancelled(true);
    }
}