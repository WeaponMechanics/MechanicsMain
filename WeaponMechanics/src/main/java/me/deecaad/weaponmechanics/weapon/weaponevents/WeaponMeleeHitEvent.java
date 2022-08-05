package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WeaponMeleeHitEvent extends WeaponEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity victim;
    private int meleeHitDelay;
    private boolean isBackstab;
    private boolean isCancelled;

    public WeaponMeleeHitEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, LivingEntity victim, int meleeHitDelay, boolean isBackstab) {
        super(weaponTitle, weaponStack, shooter);
        this.victim = victim;
        this.meleeHitDelay = meleeHitDelay;
        this.isBackstab = isBackstab;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public int getMeleeHitDelay() {
        return meleeHitDelay;
    }

    public void setMeleeHitDelay(int meleeHitDelay) {
        this.meleeHitDelay = meleeHitDelay;
    }

    public boolean isBackstab() {
        return isBackstab;
    }

    public void setBackstab(boolean backstab) {
        isBackstab = backstab;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
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
