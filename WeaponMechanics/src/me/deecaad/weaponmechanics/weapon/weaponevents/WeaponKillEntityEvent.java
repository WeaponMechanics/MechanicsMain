package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class WeaponKillEntityEvent extends WeaponEvent {

    private final LivingEntity victim;
    private final WeaponDamageEntityEvent damageEvent;

    public WeaponKillEntityEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, LivingEntity victim, WeaponDamageEntityEvent damageEvent) {
        super(weaponTitle, weaponItem, weaponUser);
        this.victim = victim;
        this.damageEvent = damageEvent;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public WeaponDamageEntityEvent getDamageEvent() {
        return damageEvent;
    }
}