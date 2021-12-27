package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ProjectileSettings implements Serializer<ProjectileSettings> {

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

    private boolean disableEntityCollisions;

    /**
     * Empty constructor to be used as serializer
     */
    public ProjectileSettings() { }

    public ProjectileSettings(EntityType projectileDisguise, ItemStack disguiseItemOrBlock, double gravity,
                              boolean removeAtMinimumSpeed, double minimumSpeed, boolean removeAtMaximumSpeed, double maximumSpeed,
                              double decrease, double decreaseInWater, double decreaseWhenRainingOrSnowing, boolean disableEntityCollisions) {
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
        this.disableEntityCollisions = disableEntityCollisions;
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
     * @return whether to remove projectile when minimum speed is reached
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
     * @return whether to remove projectile when maximum speed is reached
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

    /**
     * @return whether to skip entity collision checks
     */
    public boolean isDisableEntityCollisions() {
        return disableEntityCollisions;
    }

    @Override
    public String getKeyword() {
        return "Projectile_Settings";
    }

    @Override
    public ProjectileSettings serialize(File file, ConfigurationSection configurationSection, String path) {

        String type = configurationSection.getString(path + ".Type");
        if (type == null) {
            debug.error("You failed to specify projectile type!", StringUtil.foundAt(file, path + ".Type"));
            return null;
        }
        type = type.trim().toUpperCase();
        boolean isInvisible = type.equals("INVISIBLE");

        EntityType projectileType = null;
        ItemStack projectileItem = null;

        if (!isInvisible) {
            try {
                projectileType = EntityType.valueOf(type);
            } catch (IllegalArgumentException e) {
                debug.error(StringUtil.foundInvalid("projectile type"),
                        StringUtil.foundAt(file, path + ".Type", type),
                        StringUtil.debugDidYouMean(type, EntityType.class));
                return null;
            }
            projectileItem = new ItemSerializer().serialize(file, configurationSection, path + ".Projectile_Item_Or_Block");
            if ((projectileType == EntityType.DROPPED_ITEM || projectileType == EntityType.FALLING_BLOCK) && (projectileItem == null || projectileItem.getType() == Material.AIR)) {
                debug.error("When using " + projectileType + " you need to define valid projectile item or block.",
                        "Now there wasn't any valid item or block at path " + path + ".Projectile_Item_Or_Block",
                        StringUtil.foundAt(file, path + ".Projectile_Item_Or_Block"));
                return null;
            }
        }

        double gravity = configurationSection.getDouble(path + ".Gravity", 0.05);

        // -1 so that CustomProjectile#tick() can understand that minimum or maximum speed isn't used
        double minimumSpeed = configurationSection.getDouble(path + ".Minimum.Speed", -10.0) * 0.1;
        boolean removeAtMinimumSpeed = configurationSection.getBoolean(path + ".Minimum.Remove_Projectile_On_Speed_Reached", false);
        double maximumSpeed = configurationSection.getDouble(path + ".Maximum.Speed", -10.0) * 0.1;
        boolean removeAtMaximumSpeed = configurationSection.getBoolean(path + ".Maximum.Remove_Projectile_On_Speed_Reached", false);

        double decrease = configurationSection.getDouble(path + ".Decrease_Motion.Base", 0.99);
        double decreaseInWater = configurationSection.getDouble(path + ".Decrease_Motion.In_Water", 0.96);
        double decreaseWhenRainingOrSnowing = configurationSection.getDouble(path + ".Decrease_Motion.When_Raining_Or_Snowing", 0.98);

        debug.validate(decrease >= 0.0, "Motion multiplier MUST be positive",
                "Use 0.0 -> 1.0 to slow down, 1.0+ to speed up", StringUtil.foundAt(file, path + ".Decrease_Motion.Base"));
        debug.validate(decreaseInWater >= 0.0, "Motion multiplier MUST be positive",
                "Use 0.0 -> 1.0 to slow down, 1.0+ to speed up", StringUtil.foundAt(file, path + ".Decrease_Motion.In_Water"));
        debug.validate(decreaseWhenRainingOrSnowing >= 0.0, "Motion multiplier MUST be positive",
                "Use 0.0 -> 1.0 to slow down, 1.0+ to speed up", StringUtil.foundAt(file, path + ".Decrease_Motion.When_Raining_Or_Snowing"));

        boolean disableEntityCollisions = configurationSection.getBoolean(path + ".Disable_Entity_Collisions", false);

        return new ProjectileSettings(projectileType, projectileItem, gravity, removeAtMinimumSpeed, minimumSpeed,
                removeAtMaximumSpeed, maximumSpeed, decrease, decreaseInWater, decreaseWhenRainingOrSnowing, disableEntityCollisions);
    }
}