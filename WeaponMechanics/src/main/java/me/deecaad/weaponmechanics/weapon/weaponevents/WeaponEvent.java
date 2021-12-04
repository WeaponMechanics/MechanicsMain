package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.events.WeaponMechanicsEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public abstract class WeaponEvent extends WeaponMechanicsEvent {

    protected final String weaponTitle;
    private final ItemStack weaponStack;
    private final LivingEntity shooter;

    /**
     * Called when any weapon event is called.
     *
     * @param weaponTitle the weapon name used in event
     */
    public WeaponEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter) {
        this.weaponTitle = weaponTitle;
        this.weaponStack = weaponStack;
        this.shooter = shooter;
    }

    /**
     * @return the weapon title
     */
    public String getWeaponTitle() {
        return weaponTitle;
    }

    /**
     * @return The itemstack weapon
     */
    public ItemStack getWeaponStack() {
        return weaponStack;
    }

    /**
     * @return the living entity involved in event
     */
    public LivingEntity getShooter() {
        return this.shooter;
    }

    public EntityType getShooterType() {
        return shooter.getType();
    }
}