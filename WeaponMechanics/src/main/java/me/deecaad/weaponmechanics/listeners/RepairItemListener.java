package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.shoot.CustomDurability;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class RepairItemListener implements Listener {

    @EventHandler (ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event instanceof InventoryCreativeEvent) {
            WeaponMechanics.debug.debug("Cannot use InventoryCreativeEvent for repair item");
            return;
        }

        if (event.getClickedInventory() instanceof PlayerInventory inventory) {
            ItemStack weapon = inventory.getItem(event.getSlot());
            String weaponTitle = weapon == null ? null : CustomTag.WEAPON_TITLE.getString(weapon);


            if (weapon == null)
                return;

            // When an item is completely broken, the config can be set, so it
            // is replaced with a broken "dummy" item. This prevents people from
            // losing their hard-earned weapons. This repair system is a bit more
            // complicated, so we handle it separately.
            if (CustomTag.BROKEN_WEAPON.hasString(weapon)) {
                repairBrokenItem(event);
                return;
            }

            // Only attempt to repair guns with proper repair items
            if (weaponTitle == null || event.getCursor() == null)
                return;

            CastData cast = new CastData(WeaponMechanics.getEntityWrapper(event.getWhoClicked()));
            repair(weapon, weaponTitle, event.getCursor(), cast);
        }
    }

    /**
     * Handles repairing a completely broken item. Remember that when items are
     * completely broken, their type and meta have been completely changed to
     * a separate item.
     *
     * @param event The non-null click event involved.
     */
    public void repairBrokenItem(InventoryClickEvent event) {
        ItemStack weapon = event.getClickedInventory().getItem(event.getSlot());
        String weaponTitle = CustomTag.BROKEN_WEAPON.getString(weapon);
        CastData cast = new CastData(WeaponMechanics.getEntityWrapper(event.getWhoClicked()));
        boolean isConsumedItem = repair(weapon, weaponTitle, event.getCursor(), cast);

        // Only change back to working weapon if durability changed
        if (!isConsumedItem)
            return;

        ItemStack weaponTemplate = WeaponMechanics.getWeaponHandler().getInfoHandler().generateWeapon(weaponTitle, 1);

        // Weapon no longer exists in config
        if (weaponTemplate == null) {
            WeaponMechanics.debug.debug(event.getWhoClicked() + " has old configuration of weapon '" + weaponTitle +"'");
            return;
        }

        int durability = CustomTag.DURABILITY.getInteger(weapon);
        int maxDurability = CustomTag.MAX_DURABILITY.getInteger(weapon);
        weapon.setType(weaponTemplate.getType());
        weapon.setItemMeta(weaponTemplate.getItemMeta());
        CustomTag.DURABILITY.setInteger(weapon, durability);
        CustomTag.MAX_DURABILITY.setInteger(weapon, maxDurability);
    }

    public boolean repair(ItemStack weapon, String weaponTitle, ItemStack repairItem, CastData cast) {
        // TODO add repair kit compatibility

        Configuration config = WeaponMechanics.getConfigurations();
        CustomDurability customDurability = config.getObject(weaponTitle + ".Shoot.Custom_Durability", CustomDurability.class);

        // We already know that the given event is for a broken weapon, but it
        // is possible that the server-admin deleted the Custom_Durability
        // section of the weapon AFTER somebody obtained a broken weapon
        if (customDurability == null)
            return false;

        // Not a valid repair item... setAmount(1) is required to get by key in map.
        int availableItems = repairItem.getAmount();
        repairItem.setAmount(1);
        if (!customDurability.getRepairItems().containsKey(repairItem))
            return false;

        // Calculate how many items can possibly be consumed in order to
        // max out the weapons durability.
        int repairPerItem = customDurability.getRepairItems().get(repairItem);
        repairItem.setAmount(availableItems);
        int durability = CustomTag.DURABILITY.getInteger(weapon);
        int maxDurability = customDurability.getMaxDurability(weapon);

        // Only allow repairs
        if (maxDurability <= 0 || durability >= maxDurability) {
            if (customDurability.getDenyRepairMechanics() != null)
                customDurability.getDenyRepairMechanics().use(cast);
            return false;
        }

        // Consume items until the durability is maxed out, or until we run
        // out of items to repair with.
        while (availableItems > 0 && durability < maxDurability) {
            durability += repairPerItem;
            availableItems--;
            maxDurability = customDurability.modifyMaxDurability(weapon);
        }

        // Update durability and the repair material amount
        repairItem.setAmount(availableItems);
        CustomTag.DURABILITY.setInteger(weapon, Math.min(maxDurability, durability));
        if (customDurability.getRepairMechanics() != null)
            customDurability.getRepairMechanics().use(cast);

        return true;
    }

    @EventHandler
    public void onExp(PlayerExpChangeEvent event) {
        ItemStack weapon = event.getPlayer().getInventory().getItemInMainHand();
        String weaponTitle = !weapon.hasItemMeta() ? null : CustomTag.WEAPON_TITLE.getString(weapon);

        // Allow offhand repair as well
        if (weaponTitle == null) {
            weapon = event.getPlayer().getInventory().getItemInOffHand();
            weaponTitle = !weapon.hasItemMeta() ? null : CustomTag.WEAPON_TITLE.getString(weapon);

            // Player isn't holding any weapon
            if (weaponTitle == null)
                return;
        }

        Configuration config = WeaponMechanics.getConfigurations();
        CustomDurability customDurability = config.getObject(weaponTitle + ".Shoot.Custom_Durability", CustomDurability.class);

        // Weapon does not use durability.
        if (customDurability == null)
            return;

        int durability = CustomTag.DURABILITY.getInteger(weapon);
        int maxDurability = customDurability.getMaxDurability(weapon);

        int availableExp = event.getAmount();
        while (availableExp > 0 && durability < maxDurability) {
            durability += customDurability.getRepairPerExp();
            availableExp--;
        }

        // Repair the item and consume the experience.
        event.setAmount(availableExp);
        CustomTag.DURABILITY.setInteger(weapon, Math.min(maxDurability, durability));
    }
}
