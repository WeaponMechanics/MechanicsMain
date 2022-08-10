package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a weapon scopes in.
 */
public class WeaponScopeEvent extends WeaponEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ScopeType scopeType;
    private double zoomAmount;
    private final int zoomStack;
    private boolean isCancelled;
    
    public WeaponScopeEvent(String weaponTitle, ItemStack weaponStack, LivingEntity livingEntity, ScopeType scopeType, double zoomAmount, int zoomStack) {
        super(weaponTitle, weaponStack, livingEntity);

        this.scopeType = scopeType;
        this.zoomAmount = zoomAmount;
        this.zoomStack = zoomStack;
    }

    /**
     * Returns whether the user is scoping in, stacking a zoom, or zooming out.
     *
     * @return the non-null scope cause.
     */
    public ScopeType getScopeType() {
        return scopeType;
    }

    /**
     * The new zoom amount that has the magnification amount. Should be
     * 1..10. 1 likely means the user is scoping out (Check
     * {@link #getScopeType()}).
     *
     * @return The new zoom amount.
     */
    public double getZoomAmount() {
        return zoomAmount;
    }

    /**
     * Sets the new zoom magnification. Should be 1..10. Should probably be
     * a number greater than 1.
     *
     * @param zoomAmount The new zoom amount.
     */
    public void setZoomAmount(double zoomAmount) {
        if (zoomAmount < 1 || zoomAmount > 10) {
            throw new IllegalArgumentException("Zoom amount must be between 1 and 10");
        }
        this.zoomAmount = zoomAmount;
    }

    /**
     * You can get the old zoom stack by reducing this by 1.
     * If this is 0, it either means that stacking is not used or it was first zoom in.
     * Make sure to check scope type.
     *
     * @return the NEW zoom stack amount
     */
    public int getZoomStack() {
        return zoomStack;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum ScopeType {

        /**
         * Means zooming in
         */
        IN,

        /**
         * Means zoom is stacking
         */
        STACK,

        /**
         * Means zooming out
         */
        OUT
    }
}
