package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.file.Serializer;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public class ProjectileMotion implements Serializer<ProjectileMotion> {

    private boolean removeAtMinimumSpeed;
    private double minimumSpeed;
    private boolean removeAtMaximumSpeed;
    private double maximumSpeed;

    private double gravity;

    private double decrease;
    private double decreaseInWater;
    private double decreaseWhenRainingOrSnowing;

    /**
     * Empty constructor to be used as serializer.
     */
    public ProjectileMotion() { }

    public ProjectileMotion(double gravity, double minimumSpeed, boolean removeAtMinimumSpeed, double maximumSpeed, boolean removeAtMaximumSpeed, double decrease, double decreaseInWater, double decreaseWhenRainingOrSnowing) {
        this.minimumSpeed = minimumSpeed;
        this.removeAtMinimumSpeed = removeAtMinimumSpeed;
        this.maximumSpeed = maximumSpeed;
        this.removeAtMaximumSpeed = removeAtMaximumSpeed;
        this.gravity = gravity;
        this.decrease = decrease;
        this.decreaseInWater = decreaseInWater;
        this.decreaseWhenRainingOrSnowing = decreaseWhenRainingOrSnowing;
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
     * @return gravity of projectile
     */
    public double getGravity() {
        return gravity;
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

    @Override
    public String getKeyword() {
        return "Projectile_Motion";
    }


    @Override
    public ProjectileMotion serialize(File file, ConfigurationSection configurationSection, String path) {
        double gravity = configurationSection.getDouble(path + ".Gravity", 0.05);

        // -1 so that CustomProjectile#tick() can understand that minimum or maximum speed isn't used
        double minimumSpeed = configurationSection.getDouble(path + ".Minimum.Speed", -10.0) * 0.1;
        boolean removeAtMinimumSpeed = configurationSection.getBoolean(path + ".Minimum.Remove_Projectile_On_Speed_Reached", false);
        double maximumSpeed = configurationSection.getDouble(path + ".Maximum.Speed", -10.0) * 0.1;
        boolean removeAtMaximumSpeed = configurationSection.getBoolean(path + ".Maximum.Remove_Projectile_On_Speed_Reached", false);

        double decrease = configurationSection.getDouble(path + ".Decrease_Motion.Base", 0.99);
        double decreaseInWater = configurationSection.getDouble(path + ".Decrease_Motion.In_Water", 0.96);
        double decreaseWhenRainingOrSnowing = configurationSection.getDouble(path + ".Decrease_Motion.When_Raining_Or_Snowing", 0.98);
        return new ProjectileMotion(gravity, minimumSpeed, removeAtMinimumSpeed, maximumSpeed, removeAtMaximumSpeed, decrease, decreaseInWater, decreaseWhenRainingOrSnowing);
    }
}