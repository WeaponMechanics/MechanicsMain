package me.deecaad.weaponmechanics.weapon.repair;

import org.bukkit.inventory.ItemStack;

/**
 * An immutable repair-kit definition loaded from config.yml.
 */
public final class RepairKit {

    private final String name;
    private final ItemStack item;
    private final int repairAmount;
    private final boolean consumeItem;

    public RepairKit(String name, ItemStack item, int repairAmount, boolean consumeItem) {
        this.name = name;
        this.item = item;
        this.repairAmount = repairAmount;
        this.consumeItem = consumeItem;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public int getRepairAmount() {
        return repairAmount;
    }

    public boolean isConsumeItem() {
        return consumeItem;
    }
}
