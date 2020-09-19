package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class WeaponKillEntityEvent extends WeaponEvent implements Cancellable {

    private final LivingEntity victim;
    private final WeaponDamageEntityEvent damageEvent;
    private boolean isCancelled;

    public WeaponKillEntityEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, LivingEntity victim, WeaponDamageEntityEvent damageEvent, boolean isCancelled) {
        super(weaponTitle, weaponItem, weaponUser);
        this.victim = victim;
        this.damageEvent = damageEvent;
        this.isCancelled = isCancelled;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public WeaponDamageEntityEvent getDamageEvent() {
        return damageEvent;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}