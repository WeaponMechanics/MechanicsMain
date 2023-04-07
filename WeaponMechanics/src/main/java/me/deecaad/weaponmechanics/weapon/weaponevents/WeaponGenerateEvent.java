package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class WeaponGenerateEvent extends WeaponEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Map<String, Object> data;

    public WeaponGenerateEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, Map<String, Object> data) {
        super(weaponTitle, weaponStack, shooter, null);
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public <T> T getOrDefault(String key, T value) {
        // noinspection unchecked
        return (T) data.getOrDefault(key, value);
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
