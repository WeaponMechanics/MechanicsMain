package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WeaponPostShootEvent extends WeaponEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public WeaponPostShootEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter) {
        super(weaponTitle, weaponStack, shooter);
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}