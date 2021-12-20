package me.deecaad.weaponmechanics.weapon.newprojectile;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class ProjectileSettings {

    private EntityType projectileDisguise;
    private ItemStack disguiseItemOrBlock;

    private double gravity;

    private boolean removeAtMinimumSpeed;
    private double minimumSpeed;
    private boolean removeAtMaximumSpeed;
    private double maximumSpeed;

    private double decrease;
    private double decreaseInWater;
    private double decreaseWhenRainingOrSnowing;

    public ProjectileSettings(EntityType projectileDisguise, ItemStack disguiseItemOrBlock, double gravity,
                              boolean removeAtMinimumSpeed, double minimumSpeed, boolean removeAtMaximumSpeed, double maximumSpeed,
                              double decrease, double decreaseInWater, double decreaseWhenRainingOrSnowing) {
        this.projectileDisguise = projectileDisguise;
        this.disguiseItemOrBlock = disguiseItemOrBlock;
        this.gravity = gravity;
        this.removeAtMinimumSpeed = removeAtMinimumSpeed;
        this.minimumSpeed = minimumSpeed;
        this.removeAtMaximumSpeed = removeAtMaximumSpeed;
        this.maximumSpeed = maximumSpeed;
        this.decrease = decrease;
        this.decreaseInWater = decreaseInWater;
        this.decreaseWhenRainingOrSnowing = decreaseWhenRainingOrSnowing;
    }

    /**
     * @return the entity type this projectile should be disguised as
     */
    @Nullable
    public EntityType getProjectileDisguise() {
        return this.projectileDisguise;
    }

    /**
     * Only certain entities need this. For example falling block, entity item and so on
     *
     * @return the item stack which may be used when spawning projectile disguise
     */
    @Nullable
    public ItemStack getDisguiseItemOrBlock() {
        return disguiseItemOrBlock;
    }

    /**
     * @return gravity of projectile
     */
    public double getGravity() {
        return gravity;
    }

    /**
     * @return minimum speed of projectile
     */
    public double getMinimumSpeed() {
        return minimumSpeed;
    }

    /**
     * @return whether or not to remove projectile when minimum speed is reached
     */
    public boolean isRemoveAtMinimumSpeed() {
        return this.removeAtMinimumSpeed;
    }

    /**
     * @return maximum speed of projectile
     */
    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    /**
     * @return whether or not to remove projectile when maximum speed is reached
     */
    public boolean isRemoveAtMaximumSpeed() {
        return this.removeAtMaximumSpeed;
    }

    /**
     * @return base speed decreasing
     */
    public double getDecrease() {
        return decrease;
    }

    /**
     * @return speed decreasing in water
     */
    public double getDecreaseInWater() {
        return decreaseInWater;
    }

    /**
     * @return speed decreasing when raining or snowing
     */
    public double getDecreaseWhenRainingOrSnowing() {
        return decreaseWhenRainingOrSnowing;
    }
}