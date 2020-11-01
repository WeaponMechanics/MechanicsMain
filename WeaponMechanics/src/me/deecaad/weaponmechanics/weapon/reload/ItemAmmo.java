package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.info.WeaponConverter;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class ItemAmmo implements IAmmoType {

    private static Map<String, ItemAmmo> registeredItemAmmo;
    private final String ammoName;
    private final int maximumMagazineSize;
    private final ItemStack magazine;
    private final Mechanics notSameAmmoName;
    private final Mechanics magazineAlreadyFull;
    private final Mechanics magazineFilled;
    private final ItemStack ammo;
    private final WeaponConverter ammoConverter;

    public ItemAmmo(String ammoName, int maximumMagazineSize, ItemStack magazine, Mechanics notSameAmmoName, Mechanics magazineAlreadyFull, Mechanics magazineFilled, ItemStack ammo, WeaponConverter ammoConverter) {
        this.ammoName = ammoName;
        this.maximumMagazineSize = maximumMagazineSize;
        this.magazine = magazine;
        this.notSameAmmoName = notSameAmmoName;
        this.magazineAlreadyFull = magazineAlreadyFull;
        this.magazineFilled = magazineFilled;
        this.ammo = ammo;
        this.ammoConverter = ammoConverter;

        registerItemAmmo(this);
    }

    public static boolean hasItemAmmo(ItemAmmo itemAmmo) {
        return registeredItemAmmo != null && registeredItemAmmo.containsKey(itemAmmo.ammoName);
    }

    public static void registerItemAmmo(ItemAmmo itemAmmo) {
        if (registeredItemAmmo == null) registeredItemAmmo = new HashMap<>();
        if (registeredItemAmmo.containsKey(itemAmmo.ammoName)) {
            throw new IllegalArgumentException("Tried to register item ammo with same name... (" + itemAmmo.ammoName + ")");
        }
        registeredItemAmmo.put(itemAmmo.ammoName, itemAmmo);
    }

    public static ItemAmmo getByName(String ammoName) {
        return registeredItemAmmo != null ? registeredItemAmmo.get(ammoName) : null;
    }

    public void useNotSameAmmoName(CastData castData) {
        if (notSameAmmoName != null) notSameAmmoName.use(castData);
    }

    public void useMagazineAlreadyFull(CastData castData) {
        if (magazineAlreadyFull != null) magazineAlreadyFull.use(castData);
    }

    public void useMagazineFilled(CastData castData) {
        if (magazineFilled != null) magazineFilled.use(castData);
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

        IPlayerWrapper playerWrapper = ((IPlayerWrapper) entityWrapper);

        Player player = playerWrapper.getPlayer();
        Inventory inventory = player.getInventory();

        if (magazine != null) {
            MagazineData magazineData = getMagazineAmmo(playerWrapper, inventory, amount);

            // check if magazine ammo was found
            if (magazineData == null) return 0;

            amount = magazineData.mostAmmoInSlotMag;
            ItemStack magazineItem = inventory.getItem(magazineData.slotWithMostAmmoMag);
            int magazineItemAmount = magazineItem.getAmount();

            Integer itemAmmoLeft = TagHelper.getIntegerTag(magazineItem, CustomTag.ITEM_AMMO_LEFT);

            if (magazineItemAmount > 1) {
                magazineItem.setAmount(magazineItemAmount - 1);
                inventory.setItem(magazineData.slotWithMostAmmoMag, magazineItem);
            } else {
                inventory.setItem(magazineData.slotWithMostAmmoMag, null);
            }

            if (itemAmmoLeft > amount) {
                // Just in case magazine size and weapon mag size doesn't match...
                // Give excess as ammo items
                giveAmmo(player, itemAmmoLeft - amount);
            } else {
                // In else statement bc giveAmmo also does this
                player.updateInventory();
            }
            return amount;
        }

        Map<Integer, Integer> ammoData = getAmmo(playerWrapper, inventory, amount);

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

                magazineClone = TagHelper.setIntegerTag(magazineClone, CustomTag.ITEM_AMMO_LEFT, 0);
                ItemMeta magazineMeta = magazineClone.getItemMeta();
                magazineMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(magazineMeta.getDisplayName(), player, magazineClone, null));
                magazineMeta.setLore(PlaceholderAPI.applyPlaceholders(magazineMeta.getLore(), player, magazineClone, null));
                magazineClone.setItemMeta(magazineMeta);

                giveOrDrop(player, magazineClone);

                player.updateInventory();
                return;
            }

            // give magazine items of maximum magazine size UNTIL amount is reached
            while (amount > magazineSize) {

                magazineClone = TagHelper.setIntegerTag(magazineClone, CustomTag.ITEM_AMMO_LEFT, magazineSize);
                ItemMeta magazineMeta = magazineClone.getItemMeta();
                magazineMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(magazineMeta.getDisplayName(), player, magazineClone, null));
                magazineMeta.setLore(PlaceholderAPI.applyPlaceholders(magazineMeta.getLore(), player, magazineClone, null));
                magazineClone.setItemMeta(magazineMeta);

                giveOrDrop(player, magazineClone);

                amount -= magazineSize;
            }

            if (amount > 0) {
                // give magazine with all amount left

                magazineClone = TagHelper.setIntegerTag(magazineClone, CustomTag.ITEM_AMMO_LEFT, amount);
                ItemMeta magazineMeta = magazineClone.getItemMeta();
                magazineMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(magazineMeta.getDisplayName(), player, magazineClone, null));
                magazineMeta.setLore(PlaceholderAPI.applyPlaceholders(magazineMeta.getLore(), player, magazineClone, null));
                magazineClone.setItemMeta(magazineMeta);

                giveOrDrop(player, magazineClone);
            }
            player.updateInventory();
            return;
        }

        if (amount == 0) return;

        giveAmmo(player, amount);
    }

    private void giveAmmo(Player player, int amount) {
        // clone ammo
        ItemStack ammoClone = ammo.clone();

        // give ammo items of 64 stacks UNTIL amount is less than 64
        while (amount > 64) {
            ammoClone.setAmount(amount);
            giveOrDrop(player, ammoClone);
            amount -= 64;
        }
        if (amount > 0) {
            // give remaining amount of ammo
            ammoClone.setAmount(amount);
            giveOrDrop(player, ammoClone);
        }
        player.updateInventory();
    }

    public int getMaximumMagazineSize() {
        return maximumMagazineSize;
    }

    public boolean useMagazine() {
        return magazine != null;
    }

    private MagazineData getMagazineAmmo(IPlayerWrapper playerWrapper, Inventory inventory, int amount) {

        // Allow converting only every 10s
        boolean shouldTryConverting = ammoConverter != null && NumberUtils.hasMillisPassed(playerWrapper.getLastAmmoConvert(), 10000);
        boolean didChanges = false;
        Player player = playerWrapper.getPlayer();

        int slotWithMostAmmoMag = -1;
        int mostAmmoInSlotMag = 0;
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getType() == Material.AIR) continue;

            String itemAmmoName = TagHelper.getStringTag(itemStack, CustomTag.ITEM_AMMO_NAME);
            if (itemAmmoName == null || !itemAmmoName.equals(ammoName)) {
                if (!shouldTryConverting) continue;

                if (!ammoConverter.isMatch(itemStack, magazine)) continue;

                itemStack.setType(magazine.getType());

                ItemMeta magazineMeta = magazine.clone().getItemMeta();
                magazineMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(magazineMeta.getDisplayName(), player, itemStack, null));
                magazineMeta.setLore(PlaceholderAPI.applyPlaceholders(magazineMeta.getLore(), player, itemStack, null));
                itemStack.setItemMeta(magazineMeta);

                inventory.setItem(i, itemStack);

                didChanges = true;
                // Break since magazines have 0 ammo by default...
                break;
            }

            Integer itemAmmoLeft = TagHelper.getIntegerTag(itemStack, CustomTag.ITEM_AMMO_LEFT);
            if (itemAmmoLeft == null || itemAmmoLeft <= 0) continue;

            if (slotWithMostAmmoMag == -1 || itemAmmoLeft > mostAmmoInSlotMag) {
                slotWithMostAmmoMag = i;
                mostAmmoInSlotMag = itemAmmoLeft;

                // If slot already has required amount -> break
                if (mostAmmoInSlotMag >= amount) break;
            }
        }

        if (shouldTryConverting) {
            playerWrapper.convertedAmmo();
            if (didChanges) player.updateInventory();
        }

        return slotWithMostAmmoMag == -1 ? null : new MagazineData(slotWithMostAmmoMag, mostAmmoInSlotMag);
    }

    private Map<Integer, Integer> getAmmo(IPlayerWrapper playerWrapper, Inventory inventory, int amount) {

        // Allow converting only every 10s
        boolean shouldTryConverting = ammoConverter != null && NumberUtils.hasMillisPassed(playerWrapper.getLastAmmoConvert(), 10000);
        boolean didChanges = false;

        Map<Integer, Integer> ammoMap = new HashMap<>();
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getType() == Material.AIR) continue;

            String itemAmmoName = TagHelper.getStringTag(itemStack, CustomTag.ITEM_AMMO_NAME);
            if (itemAmmoName == null || !itemAmmoName.equals(ammoName)) {
                if (!shouldTryConverting) continue;

                if (!ammoConverter.isMatch(itemStack, ammo)) continue;

                itemStack.setType(ammo.getType());
                itemStack.setItemMeta(ammo.getItemMeta());
                inventory.setItem(i, itemStack);

                didChanges = true;
            }

            int itemStackAmount = itemStack.getAmount();

            if (amount < itemStackAmount) {
                ammoMap.put(i, amount);
                // break since enough ammo is found
                break;
            }

            ammoMap.put(i, itemStackAmount);
            amount -= itemStackAmount;
        }

        if (shouldTryConverting) {
            playerWrapper.convertedAmmo();
            if (didChanges) playerWrapper.getPlayer().updateInventory();
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