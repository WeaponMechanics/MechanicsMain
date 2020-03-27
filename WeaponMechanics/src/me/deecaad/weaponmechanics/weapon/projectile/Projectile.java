package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.general.ItemSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;

public class Projectile implements Serializer<Projectile> {

    private EntityType projectileDisguise;
    private float projectileWidth; // xz
    private float projectileLength; // y
    private ItemStack projectileStack;
    private Through through;
    private ProjectileMotion projectileMotion;

    public Projectile(EntityType projectileDisguise, float projectileWidth, float projectileLength, ItemStack projectileStack, Through through, ProjectileMotion projectileMotion) {
        this.projectileDisguise = projectileDisguise;
        this.projectileWidth = projectileWidth;
        this.projectileLength = projectileLength;
        this.projectileStack = projectileStack;
        this.through = through;
        this.projectileMotion = projectileMotion;
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
    public void shoot(LivingEntity entity, Location location, Vector motion) {
        CustomProjectilesRunnable.addProjectile(new CustomProjectile(this, entity, location, motion));
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
     * Projectile's length from center to upwards/downwards.
     * Better known as Y.
     *
     * @return the projectile length
     */
    public float getProjectileLength() {
        return projectileLength;
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
        boolean isInvisible = type.equalsIgnoreCase("INVISIBLE");

        EntityType projectileType = null;
        ItemStack projectileItem = null;

        if (!isInvisible) {
            try {
                projectileType = EntityType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                DebugUtil.log(LogLevel.ERROR,
                        "Found an invalid projectile type in configurations!",
                        "Located at file " + file + " in " + path + ".Settings.Type (" + type.toUpperCase() + ") in configurations");
                return null;
            }
            projectileItem = new ItemSerializer().serialize(file, configurationSection, path + ".Projectile_Item_Or_Block");
            if ((projectileType == EntityType.DROPPED_ITEM || projectileType == EntityType.FALLING_BLOCK) && (projectileItem == null || projectileItem.getType() == Material.AIR)) {
                DebugUtil.log(LogLevel.ERROR,
                        "When using " + projectileType + " you need to define valid projectile item or block.",
                        "Now there wasn't any valid item or block at path " + path + ".Projectile_Item_Or_Block",
                        "Located at file " + file + " in " + path + ".Projectile_Item_Or_Block in configurations");
                return null;
            }
        }

        // Gives default values in case these are missing
        float width = (float) configurationSection.getDouble(path + ".Settings.Width", 0.25F);
        float height = (float) configurationSection.getDouble(path + ".Settings.Height", 0.25F);

        ProjectileMotion projectileMotion = new ProjectileMotion().serialize(file, configurationSection, path + ".Projectile_Motion");
        Through through = new Through().serialize(file, configurationSection, path + ".Through");
        return new Projectile(projectileType, width, height, projectileItem, through, projectileMotion);
    }
}