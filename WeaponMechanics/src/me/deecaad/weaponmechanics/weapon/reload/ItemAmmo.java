package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.info.WeaponConverter;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemAmmo implements IAmmoType {

    private String ammoName;
    private ItemStack magazine;
    private ItemStack ammo;
    private WeaponConverter ammoConverter;

    public ItemAmmo(String ammoName, ItemStack magazine, ItemStack ammo, WeaponConverter ammoConverter) {
        this.ammoName = ammoName;
        this.magazine = magazine;
        this.ammo = ammo;
        this.ammoConverter = ammoConverter;
    }

    @Override
    public boolean hasAmmo(IEntityWrapper entityWrapper) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return false;
        Inventory inventory = ((IPlayerWrapper) entityWrapper).getPlayer().getInventory();
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);

            String itemAmmoName = TagHelper.getStringTag(itemStack, CustomTag.ITEM_AMMO_NAME);
            if (itemAmmoName == null || !itemAmmoName.equals(ammoName)) continue;

            if (magazine != null) {
                Integer itemAmmoLeft = TagHelper.getIntegerTag(itemStack, CustomTag.ITEM_AMMO_LEFT);
                if (itemAmmoLeft != null && itemAmmoLeft > 0) {
                    // has at least one (magazine) ammo
                    return true;
                }
                continue;
            }

            // has at least one (non-magazine) ammo found since code reached this point
            return true;
        }
        return false;
    }

    @Override
    public int getAmount(IEntityWrapper entityWrapper, int magazineSize) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;

        int foundAmount = 0;

        Inventory inventory = ((IPlayerWrapper) entityWrapper).getPlayer().getInventory();
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);

            String itemAmmoName = TagHelper.getStringTag(itemStack, CustomTag.ITEM_AMMO_NAME);
            if (itemAmmoName == null || !itemAmmoName.equals(ammoName)) continue;

            Integer itemAmmoLeft = TagHelper.getIntegerTag(itemStack, CustomTag.ITEM_AMMO_LEFT);
            if (itemAmmoLeft == null) {
                // non-magazine
                foundAmount += itemStack.getAmount();
            } else {
                // magazine
                foundAmount += itemStack.getAmount() * itemAmmoLeft;
            }
        }

        return foundAmount;
    }

    @Override
    public int remove(IEntityWrapper entityWrapper, int amount, int magazineSize) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;
        if (amount == 0) return 0;

        Player player = ((IPlayerWrapper) entityWrapper).getPlayer();
        Inventory inventory = player.getInventory();

        if (magazine != null) {
            MagazineData magazineData = getMagazineAmmo(inventory, amount);

            // check if magazine ammo was found
            if (magazineData == null) return 0;

            amount = magazineData.mostAmmoInSlotMag;
            ItemStack magazineItem = inventory.getItem(magazineData.slotWithMostAmmoMag);
            int magazineItemAmount = magazineItem.getAmount();
            if (magazineItemAmount > 1) {
                magazineItem.setAmount(magazineItemAmount - 1);
                inventory.setItem(magazineData.slotWithMostAmmoMag, magazineItem);
            } else {
                inventory.setItem(magazineData.slotWithMostAmmoMag, null);
            }

            player.updateInventory();
            return amount;
        }

        Map<Integer, Integer> ammoData = getAmmo(inventory, amount);

        // check if ammo was found
        if (ammoData == null) return 0;

        int finalAmount = 0;
        for (Map.Entry<Integer, Integer> ammoEntry : ammoData.entrySet()) {
            int slot = ammoEntry.getKey();
            int value = ammoEntry.getValue();
            ItemStack ammoItem = inventory.getItem(slot);
            int ammoItemAmount = ammoItem.getAmount();

            if (ammoItemAmount != value) {
                ammoItem.setAmount(ammoItemAmount - value);
                inventory.setItem(slot, ammoItem);
            } else {
                inventory.setItem(slot, null);
            }

            finalAmount += value;
        }

        player.updateInventory();
        return finalAmount;
    }

    @Override
    public void give(IEntityWrapper entityWrapper, int amount, int magazineSize) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return;
        Player player = ((IPlayerWrapper) entityWrapper).getPlayer();

        if (magazine != null) {

            // clone magazine
            ItemStack magazineClone = magazine.clone();

            if (amount == 0) {
                // To give empty magazine back
                giveOrDrop(player, TagHelper.setIntegerTag(magazineClone, CustomTag.ITEM_AMMO_LEFT, 0));
                player.updateInventory();
                return;
            }

            // give magazine items of maximum magazine size UNTIL amount is reached
            while (amount > magazineSize) {
                giveOrDrop(player, TagHelper.setIntegerTag(magazineClone, CustomTag.ITEM_AMMO_LEFT, magazineSize));
                amount -= magazineSize;
            }
            if (amount <= 0) return;

            // give magazine with all amount left
            giveOrDrop(player, TagHelper.setIntegerTag(magazineClone, CustomTag.ITEM_AMMO_LEFT, amount));
            player.updateInventory();
            return;
        }

        if (amount == 0) return;

        // clone ammo
        ItemStack ammoClone = ammo.clone();

        // give ammo items of 64 stacks UNTIL amount is less than 64
        while (amount > 64) {
            ammoClone.setAmount(amount);
            giveOrDrop(player, ammoClone);
            amount -= 64;
        }
        if (amount <= 0) return;

        // give remaining amount of ammo
        ammoClone.setAmount(amount);
        giveOrDrop(player, ammoClone);
        player.updateInventory();
    }

    public boolean useMagazine() {
        return magazine != null;
    }

    private MagazineData getMagazineAmmo(Inventory inventory, int amount) {

        // todo handle convert

        int slotWithMostAmmoMag = -1;
        int mostAmmoInSlotMag = 0;
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);

            String itemAmmoName = TagHelper.getStringTag(itemStack, CustomTag.ITEM_AMMO_NAME);
            if (itemAmmoName == null || !itemAmmoName.equals(ammoName)) continue;

            Integer itemAmmoLeft = TagHelper.getIntegerTag(itemStack, CustomTag.ITEM_AMMO_LEFT);
            if (itemAmmoLeft == null || itemAmmoLeft <= 0) continue;

            if (slotWithMostAmmoMag == -1 || itemAmmoLeft > mostAmmoInSlotMag) {
                slotWithMostAmmoMag = i;
                mostAmmoInSlotMag = itemAmmoLeft;

                // If slot already has required amount -> break
                if (mostAmmoInSlotMag >= amount) break;
            }
        }
        return slotWithMostAmmoMag == -1 ? null : new MagazineData(slotWithMostAmmoMag, mostAmmoInSlotMag);
    }

    private Map<Integer, Integer> getAmmo(Inventory inventory, int amount) {

        // todo handle convert

        Map<Integer, Integer> ammoMap = new HashMap<>();
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);

            String itemAmmoName = TagHelper.getStringTag(itemStack, CustomTag.ITEM_AMMO_NAME);
            if (itemAmmoName == null || !itemAmmoName.equals(ammoName)) continue;

            int itemStackAmount = itemStack.getAmount();

            if (amount < itemStackAmount) {
                ammoMap.put(i, amount);
                // break since enough ammo is found
                break;
            }

            ammoMap.put(i, itemStackAmount);
            amount -= itemStackAmount;

        }
        return ammoMap.isEmpty() ? null : ammoMap;
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

    private static class MagazineData {

        public int slotWithMostAmmoMag;
        public int mostAmmoInSlotMag;

        public MagazineData(int slotWithMostAmmoMag, int mostAmmoInSlotMag) {
            this.slotWithMostAmmoMag = slotWithMostAmmoMag;
            this.mostAmmoInSlotMag = mostAmmoInSlotMag;
        }
    }
}