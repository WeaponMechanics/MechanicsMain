package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.weaponmechanics.utils.CustomTag;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class MagazineMergingListeners implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (item == null || item.getItemMeta() == null) return;
        if (cursor == null || cursor.getItemMeta() == null) return;

        String itemAmmoName = CustomTag.AMMO_TITLE.getString(item);
        String cursorAmmoName = CustomTag.AMMO_TITLE.getString(cursor);

        if (!Objects.equals(itemAmmoName, cursorAmmoName) || itemAmmoName == null) return;

        Ammo ammo = AmmoRegistry.AMMO_REGISTRY.get(itemAmmoName);
        if (ammo == null) return;

        IAmmoType type = ammo.getType();
        if (!(type instanceof ItemAmmo itemAmmo)) return;

        Magazine magazine = itemAmmo.getMagazine();
        if (magazine == null) return;

        int itemAmmoAmount = Magazine.getAmmoFromItem(item);
        int cursorAmmoAmount = Magazine.getAmmoFromItem(cursor);

        int capacity = Magazine.getCapacity(item);
        int cursorCapacity = Magazine.getCapacity(cursor);
        if (capacity != cursorCapacity) return;
        if (capacity == itemAmmoAmount || cursorCapacity == cursorAmmoAmount) return;

        itemAmmoAmount += cursorAmmoAmount;
        cursorAmmoAmount = Math.max(itemAmmoAmount - capacity, 0);
        itemAmmoAmount = Math.min(itemAmmoAmount, capacity);

        ItemStack newItem = magazine.toItem(itemAmmoAmount, capacity);
        ItemStack newCursor = cursorAmmoAmount == 0 ? new ItemStack(Material.AIR) : magazine.toItem(cursorAmmoAmount, capacity);

        event.setCurrentItem(newItem);
        event.getWhoClicked().setItemOnCursor(newCursor);
        event.setCancelled(true);
    }
}
