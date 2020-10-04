package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * @see org.bukkit.event.entity.EntityPickupItemEvent
 */
public class WeaponPickupEvent extends WeaponEvent {

    private final Item item;

    public WeaponPickupEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, @Nullable Item item) {
        super(weaponTitle, weaponStack, shooter);
        this.item = item;
    }

    @Nullable
    public Item getItem() {
        return item;
    }
}