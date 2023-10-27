package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Magazine {
    private final ItemAmmo itemAmmo;
    private final ItemStack baseItem;
    private final int capacity;

    public Magazine(ItemAmmo itemAmmo, ItemStack baseItem, int capacity) {
        this.itemAmmo = itemAmmo;
        this.baseItem = baseItem;
        this.capacity = capacity;
    }

    public static int getAmmoFromItem(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        return dataContainer.getOrDefault(new NamespacedKey(WeaponMechanics.getPlugin(), "ammo"), PersistentDataType.INTEGER, 0);
    }

    public ItemStack getBaseItem() {
        return baseItem;
    }

    public int getCapacity() {
        return capacity;
    }

    public ItemStack toItem() {
        return toItem(capacity);
    }

    public ItemStack toItem(int ammoAmount) {
        ItemStack item = baseItem.clone();
        ItemMeta itemMeta = item.getItemMeta();

        List<String> lore = itemMeta.getLore() == null ? new ArrayList<>() : new ArrayList<>(itemMeta.getLore());
        lore.add(ChatColor.GRAY + "Capacity: " + ChatColor.GREEN + ammoAmount + "/" + capacity);
        itemMeta.setLore(lore);

        if (ammoAmount < capacity && itemMeta instanceof Damageable damageable) {
            short maxDurability = item.getType().getMaxDurability();
            float usedAmmoPercentage = (float) (capacity - ammoAmount) / capacity;
            damageable.setDamage((int) (usedAmmoPercentage * maxDurability));
        }

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(new NamespacedKey(WeaponMechanics.getPlugin(), "ammo"), PersistentDataType.INTEGER, ammoAmount);

        //To makes magazines of same amount of ammo unstackable
        dataContainer.set(new NamespacedKey(WeaponMechanics.getPlugin(), "uuid"), PersistentDataType.STRING, UUID.randomUUID().toString());

        item.setItemMeta(itemMeta);
        return item;
    }
}
