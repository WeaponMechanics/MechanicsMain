package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemAmmo implements IAmmoType {

    // Defined in ammo types list
    private String ammoName;

    private String symbol;
    private ItemStack bulletItem;
    private ItemStack magazineItem;
    private AmmoConverter ammoConverter;

    public ItemAmmo(String ammoName, String symbol, ItemStack bulletItem, ItemStack magazineItem, AmmoConverter ammoConverter) {
        this.ammoName = ammoName;
        this.symbol = symbol;
        this.bulletItem = bulletItem;
        this.magazineItem = magazineItem;
        this.ammoConverter = ammoConverter;
    }

    @Override
    public String getAmmoName() {
        return ammoName;
    }

    @Override
    public String getSymbol() {
        return symbol != null ? symbol : ammoName;
    }

    @Override
    public boolean hasAmmo(PlayerWrapper playerWrapper) {
        Player player = playerWrapper.getPlayer();
        PlayerInventory playerInventory = player.getInventory();

        // Only check hotbar slots and normal inventory slots meaning slots from 0 to 35
        for (int i = 0; i < 36; ++i) {

            // Don't check currently held slot either since it's weapon
            if (i == playerInventory.getHeldItemSlot()) continue;

            ItemStack potentialAmmo = playerInventory.getItem(i);
            if (potentialAmmo == null || potentialAmmo.getType() == Material.AIR) continue;

            String potentialAmmoName = CustomTag.AMMO_NAME.getString(potentialAmmo);
            if (potentialAmmoName == null || !potentialAmmoName.equals(ammoName)) continue;

            // Yay found at least one ammo
            return true;
        }

        // This is called after the checks above since we don't want
        // to try converting if there is still ammo in inventory

        // Allow converting only every 10s
        if (ammoConverter == null || !NumberUtil.hasMillisPassed(playerWrapper.getLastAmmoConvert(), 10000)) return false;

        // We will want to convert whole inventory when this occurs
        boolean foundAmmo = false;

        // Only check hotbar slots and normal inventory slots meaning slots from 0 to 35
        for (int i = 0; i < 36; ++i) {

            // Don't check currently held slot either since it's weapon
            if (i == playerInventory.getHeldItemSlot()) continue;

            ItemStack potentialAmmo = playerInventory.getItem(i);
            if (potentialAmmo == null || potentialAmmo.getType() == Material.AIR
                    || CustomTag.AMMO_NAME.getString(potentialAmmo) != null) continue;

            ItemStack convertTo = null;
            if (bulletItem != null && ammoConverter.isMatch(potentialAmmo, bulletItem)) {
                convertTo = bulletItem.clone();
            } else if (magazineItem != null && ammoConverter.isMatch(potentialAmmo, magazineItem)) {
                convertTo = magazineItem.clone();
            }

            // Wasn't valid ammo, check next slot
            if (convertTo == null) continue;

            potentialAmmo.setType(convertTo.getType());
            potentialAmmo.setItemMeta(convertTo.getItemMeta());
            updatePlaceholders(potentialAmmo, player);

            playerInventory.setItem(i, potentialAmmo);

            foundAmmo = true;
        }

        // Update the convert time
        playerWrapper.convertedAmmo();
        return foundAmmo;
    }

    @Override
    public int removeAmmo(ItemStack weaponStack, PlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        PlayerInventory playerInventory = playerWrapper.getPlayer().getInventory();

        int foundMagazineSlot = -1;

        List<Integer> bulletItemIndexes = null;
        int foundBulletItemAmount = 0;

        // Only check hotbar slots and normal inventory slots meaning slots from 0 to 35
        for (int i = 0; i < 36; ++i) {

            // Don't check currently held slot either since it's weapon
            if (i == playerInventory.getHeldItemSlot()) continue;

            ItemStack potentialAmmo = playerInventory.getItem(i);
            if (potentialAmmo == null || potentialAmmo.getType() == Material.AIR) continue;

            String potentialAmmoName = CustomTag.AMMO_NAME.getString(potentialAmmo);
            if (potentialAmmoName == null || !potentialAmmoName.equals(ammoName)) continue;

            if (CustomTag.AMMO_MAGAZINE.getInteger(potentialAmmo) == 1) {
                // Magazine item

                // If its already been found -> continue
                if (foundMagazineSlot != -1) continue;

                // Update this to first found magazine slot
                foundMagazineSlot = i;

                // If reloaded from empty clip, then amount is always maximum magazine size
                if (bulletItem == null || amount >= maximumMagazineSize) {
                    // Since reloaded from empty clip or bullet item isn't used,
                    // and we found magazine item, consume the item and reload as full
                    consumeItem(playerInventory, i, potentialAmmo, 1);

                    return amount;
                }
            } else if (bulletItem != null) {
                // Bullet item

                int currentItemAmount = potentialAmmo.getAmount();

                // Let's check if current item amount alone is enough
                if (currentItemAmount >= amount) {
                    consumeItem(playerInventory, i, potentialAmmo, amount);
                    return amount;
                }

                foundBulletItemAmount += currentItemAmount;
                if (foundBulletItemAmount < amount) {
                    if (bulletItemIndexes == null) bulletItemIndexes = new ArrayList<>(2);
                    bulletItemIndexes.add(i);

                    // Keep searching...
                    continue;
                }

                // Here we know we've found enough bullet items to reload
                break;
            }
        }

        if (foundBulletItemAmount < amount && foundMagazineSlot != -1) {
            // Consume the magazine found at magazine slot
            //noinspection ConstantConditions
            consumeItem(playerInventory, foundMagazineSlot, playerInventory.getItem(foundMagazineSlot), 1);

            return amount;
        }

        if (foundBulletItemAmount == 0) return 0;

        int consumed = 0;

        // Consume the found bullet item ammo from bullet item indexes
        for (int i : bulletItemIndexes) {
            ItemStack bulletItem = playerInventory.getItem(i);
            //noinspection ConstantConditions
            int currentItemAmount = bulletItem.getAmount();

            if (currentItemAmount < amount) {
                // Since this happened we can just set the slot item to null,
                // and stay on track how many bullet items have been consumed,
                // and how many we still have to remove

                playerInventory.setItem(i, null);
                consumed += currentItemAmount;
                amount -= currentItemAmount;
                continue;
            }

            // If the player didn't have enough bullet items to fill whole magazine,
            // the code won't never even reach this point.

            // Here we know that this slot has equal to or more than amount we still need to remove

            consumed += amount;
            consumeItem(playerInventory, i, bulletItem, amount);

            // This should be last index of list anyway, but let's still make sure to use break here
            break;
        }

        return consumed;
    }

    private void consumeItem(PlayerInventory playerInventory, int index, ItemStack itemStack, int amount) {
        int itemAmount = itemStack.getAmount();
        if (itemAmount > amount) {
            itemStack.setAmount(itemAmount - amount);
            playerInventory.setItem(index, itemStack);
        } else {
            playerInventory.setItem(index, null);
        }
    }

    @Override
    public void giveAmmo(ItemStack weaponStack, PlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        Player player = playerWrapper.getPlayer();

        if (magazineItem != null) {
            int magazinesGiveAmount = amount / maximumMagazineSize;
            if (magazinesGiveAmount > 0) {
                giveOrDrop(player, magazineItem.clone(), magazinesGiveAmount);
            }

            // Give rest of the ammo as bullet items back if defined
            if (bulletItem != null) {
                int remainder = amount % maximumMagazineSize;
                if (remainder > 0) {
                    giveOrDrop(player, bulletItem.clone(), remainder);
                }
            }
            return;
        }

        // Magazines weren't used so simply give bullet items using given amount
        giveOrDrop(player, bulletItem.clone(), amount);
    }

    @Override
    public int getMaximumAmmo(PlayerWrapper playerWrapper, int maximumMagazineSize) {
        PlayerInventory playerInventory = playerWrapper.getPlayer().getInventory();

        int amount = 0;

        // Only check hotbar slots and normal inventory slots meaning slots from 0 to 35
        for (int i = 0; i < 36; ++i) {

            // Don't check currently held slot either since it's weapon
            if (i == playerInventory.getHeldItemSlot()) continue;

            ItemStack potentialAmmo = playerInventory.getItem(i);
            if (potentialAmmo == null || potentialAmmo.getType() == Material.AIR) continue;

            String potentialAmmoName = CustomTag.AMMO_NAME.getString(potentialAmmo);
            if (potentialAmmoName == null || !potentialAmmoName.equals(ammoName)) continue;

            // Now we know it's actually an ammo item
            if (CustomTag.AMMO_MAGAZINE.getInteger(potentialAmmo) == 1) {
                amount += (potentialAmmo.getAmount() * maximumMagazineSize);
            } else {
                amount +=  potentialAmmo.getAmount();
            }
        }
        return amount;
    }

    private void giveOrDrop(Player player, ItemStack itemStack, int amount) {

        // More compatible this way if this gets changed in the future
        int maximumStackSize = player.getInventory().getMaxStackSize();

        while (amount > maximumStackSize) {

            // Keep cloning since we do modifications for same item constantly
            // while adding it to player inventory
            ItemStack clone = itemStack.clone();

            clone.setAmount(amount);
            giveOrDrop(player, clone);
            amount -= maximumStackSize;
        }

        if (amount > 0) {

            // Given item stack is cloned in all cases
            itemStack.setAmount(amount);

            giveOrDrop(player, itemStack);
        }
    }

    private void giveOrDrop(Player player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();
        updatePlaceholders(itemStack, player);

        // Check if inventory doesn't have any free slots
        if (inventory.firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation().add(0.0, 1.0, 0.0), itemStack);
            return;
        }
        inventory.addItem(itemStack);
    }

    private void updatePlaceholders(ItemStack itemStack, Player player) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;

        itemMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(itemMeta.getDisplayName(), player, itemStack, null));
        itemMeta.setLore(PlaceholderAPI.applyPlaceholders(itemMeta.getLore(), player, itemStack, null));
        itemStack.setItemMeta(itemMeta);
    }
}
