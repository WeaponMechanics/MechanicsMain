package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Projectile implements Serializer<Projectile> {

    private EntityType projectileDisguise;
    private float projectileWidth; // xz
    private float projectileHeight; // y
    private ItemStack projectileStack;
    private Through through;
    private ProjectileMotion projectileMotion;
    private Sticky sticky;
    private Bouncy bouncy;

    public Projectile(@Nonnull ProjectileMotion projectileMotion, @Nullable EntityType projectileDisguise,
                      float projectileWidth, float projectileHeight, @Nullable ItemStack projectileStack,
                      @Nullable Through through, @Nullable Sticky sticky, @Nullable Bouncy bouncy) {
        this.projectileDisguise = projectileDisguise;
        if (projectileWidth <= 0) throw new IllegalArgumentException("Projectile width can't be 0 or less");
        if (projectileHeight <= 0) throw new IllegalArgumentException("Projectile height can't be 0 or less");
        this.projectileWidth = projectileWidth;
        this.projectileHeight = projectileHeight;
        this.projectileStack = projectileStack;
        this.through = through;
        if (projectileMotion == null) throw new IllegalArgumentException("Projectile motion can't be null!");
        this.projectileMotion = projectileMotion;
        this.sticky = sticky;
        this.bouncy = bouncy;
    }

    /**
     * Empty constructor to be used as serializer
     */
    public Projectile() { }

    /**
     * Shoots this projectile with given location and motion
     *
     * @param entity the living entity used to shoot
     * @param location the location from where to shoot
     * @param motion the motion of projectile
     */
    public ICustomProjectile shoot(LivingEntity entity, Location location, Vector motion) {
        CustomProjectile projectile = new CustomProjectile(this, entity, location, motion);
        WeaponMechanics.getCustomProjectilesRunnable().addProjectile(projectile);
        return projectile;
    }

    /**
     * Shoots this projectile with given location and motion
     *
     * @param entity the living entity used to shoot
     * @param location the location from where to shoot
     * @param motion the motion of projectile
     * @param weaponStack the weapon stack used to shoot
     * @param weaponTitle the weapon title used to shoot
     */
    public ICustomProjectile shoot(LivingEntity entity, Location location, Vector motion, @Nullable ItemStack weaponStack, @Nullable String weaponTitle) {
        CustomProjectile projectile = new CustomProjectile(this, entity, location, motion, weaponStack, weaponTitle);
        WeaponMechanics.getCustomProjectilesRunnable().addProjectile(projectile);
        return projectile;
    }

    /**
     * @return the entity type this projectile should be disguised as
     */
    public EntityType getProjectileDisguise() {
        return projectileDisguise;
    }

    /**
     * Projectile's width from center to forwards/backwards and sideways.
     * Better known as XZ.
     *
     * @return the projectile width
     */
    public float getProjectileWidth() {
        return projectileWidth;
    }

    /**
     * Projectile's height from center to upwards/downwards.
     * Better known as Y.
     *
     * @return the projectile height
     */
    public float getProjectileHeight() {
        return projectileHeight;
    }

    /**
     * Only certain entities need this. For example falling block, entity item and so on
     *
     * @return the item stack which may be used when spawning projectile disguise
     */
    public ItemStack getProjectileStack() {
        return projectileStack;
    }

    /**
     * @return the projectile through settings
     */
    public Through getThrough() {
        return through;
    }

    /**
     * @return the projectile sticky settings
     */
    public Sticky getSticky() {
        return sticky;
    }

    /**
     * @return the projectile bouncy settings
     */
    public Bouncy getBouncy() {
        return bouncy;
    }

    /**
     * @return the projectile motion settings
     */
    public ProjectileMotion getProjectileMotion() {
        return projectileMotion;
    }

    @Override
    public String getKeyword() {
        return "Projectile";
    }

    @Override
    public Projectile serialize(File file, ConfigurationSection configurationSection, String path) {
        String type = configurationSection.getString(path + ".Settings.Type");
        if (type == null) {
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
                debug.log(LogLevel.ERROR,
                        StringUtils.foundInvalid("projectile type"),
                        StringUtils.foundAt(file, path + ".Settings.Type", type),
                        StringUtils.debugDidYouMean(type, EntityType.class));
                return null;
            }
            projectileItem = new ItemSerializer().serialize(file, configurationSection, path + ".Projectile_Item_Or_Block");
            if ((projectileType == EntityType.DROPPED_ITEM || projectileType == EntityType.FALLING_BLOCK) && (projectileItem == null || projectileItem.getType() == Material.AIR)) {
                debug.log(LogLevel.ERROR,
                        "When using " + projectileType + " you need to define valid projectile item or block.",
                        "Now there wasn't any valid item or block at path " + path + ".Projectile_Item_Or_Block",
                        "Located at file " + file + " in " + path + ".Projectile_Item_Or_Block in configurations");
                return null;
            }
        }

        float width = (float) configurationSection.getDouble(path + ".Settings.Width");
        float height = (float) configurationSection.getDouble(path + ".Settings.Height");

        // Gives default values in case these are missing
        if (width < 0.0 || height < 0.0) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid height or width in configurations!",
                    "Located at file " + file + " in " + path + ".Settings (" + type.toUpperCase() + ") in configurations",
                    "Please make sure that they aren't less than 0.");
            return null;
        }

        if (width == 0 || height == 0) {
            if (projectileType != null) {
                double[] defaultSize = WeaponCompatibilityAPI.getProjectileCompatibility().getDefaultWidthAndHeight(projectileType);
                if (width <= 0) width = (float) defaultSize[0];
                if (height <= 0) height = (float) defaultSize[1];
            } else {
                // Give default values of 0.25 if projectile disguise isn't used
                if (width <= 0) width = 0.25f;
                if (height <= 0) height = 0.25f;
            }
        }

        ProjectileMotion projectileMotion = new ProjectileMotion().serialize(file, configurationSection, path + ".Projectile_Motion");
        Through through = new Through().serialize(file, configurationSection, path + ".Through");
        Sticky sticky = new Sticky().serialize(file, configurationSection, path + ".Sticky");
        Bouncy bouncy = new Bouncy().serialize(file, configurationSection, path + ".Bouncy");
        return new Projectile(projectileMotion, projectileType, width, height, projectileItem, through, sticky, bouncy);
    }
}