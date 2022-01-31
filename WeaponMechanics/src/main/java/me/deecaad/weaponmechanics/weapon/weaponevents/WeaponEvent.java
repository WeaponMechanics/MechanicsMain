package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public abstract class WeaponEvent extends EntityEvent {

    protected final String weaponTitle;
    private final ItemStack weaponStack;
    private final LivingEntity shooter;

    public WeaponEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter) {
        super(shooter);

        this.weaponTitle = weaponTitle;
        this.weaponStack = weaponStack;
        this.shooter = shooter;
    }

    /**
     * Returns the weapon-title associated with the weapon involved. This is
     * the same as`CustomTag.WEAPON_TITLE.getString(event.getWeaponStack())`.
     *
     * @return The non-null weapon title.
     */
    @Nonnull
    public String getWeaponTitle() {
        return weaponTitle;
    }

    /**
     * Returns the weapon item which caused the event. This should always be an
     * item in the player's main hand, or off hand.
     *
     * @return The non-null weapon item.
     */
    @Nonnull
    public ItemStack getWeaponStack() {
        return weaponStack;
    }

    /**
     * Returns the shooter involved in this event. This is the same as
     * {@link #getEntity()} but without the added overhead from casting it
     * to a {@link LivingEntity}.
     *
     * @return The non-null entity that fired the weapon. This will not
     * always be a player!
     */
    @Nonnull
    public LivingEntity getShooter() {
        return this.shooter;
    }
}