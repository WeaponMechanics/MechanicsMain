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
            ItemStack repairItem = event.getCursor();

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
            if (weaponTitle == null || repairItem == null)
                return;

            Configuration config = WeaponMechanics.getConfigurations();
            CustomDurability customDurability = config.getObject(weaponTitle + ".Shoot.Custom_Durability", CustomDurability.class);

            // Gun does not use durability
            if (customDurability == null)
                return;

            // TODO add repair kit compatibility

            // Not a valid repair item... setAmount(1) is required to get by key in map.
            int availableItems = repairItem.getAmount();
            repairItem.setAmount(1);
            if (!customDurability.getRepairItems().containsKey(repairItem))
                return;

            // Calculate how many items can possibly be consumed in order to
            // max out the weapons durability.
            int repairPerItem = customDurability.getRepairItems().get(repairItem);
            repairItem.setAmount(availableItems);
            int durability = CustomTag.DURABILITY.getInteger(weapon);
            int maxDurability = customDurability.getMaxDurability(weapon);

            // Only allow repairs
            CastData cast = new CastData(WeaponMechanics.getEntityWrapper(event.getWhoClicked()));
            if (maxDurability <= 0 || durability >= maxDurability) {
                if (customDurability.getDenyRepairMechanics() != null)
                    customDurability.getDenyRepairMechanics().use(cast);
                return;
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

            event.setCancelled(true);
        }
    }

    /**
     * Handles repairing a completely broken item.
     *
     * @param event The non-null click event involved.
     */
    public void repairBrokenItem(InventoryClickEvent event) {

    }

    @EventHandler
    public void onExp(PlayerExpChangeEvent event) {
        ItemStack weapon = event.getPlayer().getInventory().getItemInMainHand();
        String weaponTitle = CustomTag.WEAPON_TITLE.getString(weapon);

        // Allow offhand repair as well
        if (weaponTitle == null) {
            weapon = event.getPlayer().getInventory().getItemInOffHand();
            weaponTitle = CustomTag.WEAPON_TITLE.getString(weapon);

            // Player isn't holding any weapon
            if (weaponTitle == null)
                return;
        }

        Configuration config = WeaponMechanics.getConfigurations();
        CustomDurability customDurability = config.getObject(weaponTitle + ".Shoot.Custom_Durability", CustomDurability.class);

        // Weapon does not use durability.
        if (customDurability == null)
            return;

        // First we need to determine how many EXP points the weapon is able
        // to convert to durability (without overflowing too much).
        int durability = CustomTag.DURABILITY.getInteger(weapon);
        int maxDurability = customDurability.getMaxDurability(weapon);
        int consumeExp = (int) Math.ceil((maxDurability - durability) / (double) customDurability.getRepairPerExp());

        int availableExp = event.getAmount();
        int accumulate = 0;
        while (availableExp > 0 && consumeExp > 0) {
            accumulate += customDurability.getRepairPerExp();
            availableExp--;
            consumeExp--;
        }

        // Repair the item and consume the experience.
        event.setAmount(availableExp);
        CustomTag.DURABILITY.setInteger(weapon, Math.min(maxDurability, durability + accumulate));
    }
}
