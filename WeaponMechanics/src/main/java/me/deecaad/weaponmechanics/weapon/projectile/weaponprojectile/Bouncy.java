package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.file.Serializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.io.File;

public class Bouncy implements Serializer<Bouncy> {

    public static final double REQUIRED_MOTION_TO_BOUNCE = 0.3;

    private int maximumBounceAmount;
    private ListHolder<Material> blocks;
    private ListHolder<EntityType> entities;

    /**
     * Empty for serializers
     */
    public Bouncy() { }

    public Bouncy(int maximumBounceAmount, ListHolder<Material> blocks, ListHolder<EntityType> entities) {
        this.maximumBounceAmount = maximumBounceAmount;
        this.blocks = blocks;
        this.entities = entities;
    }

    /**
     * @param projectile the projectile
     * @param hit the hit entity or block
     * @return true if projectile bounced, false if projectile should die
     */
    public boolean handleBounce(WeaponProjectile projectile, RayTraceResult hit) {
        if (projectile.getMotionLength() < REQUIRED_MOTION_TO_BOUNCE) return false;

        Double speedModifier;
        if (hit.isBlock()) {
            speedModifier = blocks != null ? blocks.isValid(hit.getBlock().getType()) : null;
        } else {
            speedModifier = entities != null ? entities.isValid(hit.getLivingEntity().getType()) : null;
        }

        // Speed modifier null would mean that it wasn't valid material or entity type
        if (speedModifier == null || maximumBounceAmount - projectile.getBounces() < 1) {
            // Projectile should die
            return false;
        }

        Vector motion = projectile.getMotion();
        if (speedModifier != 1.0) motion.multiply(speedModifier);

        switch (hit.getHitFace()) {
            case UP: case DOWN:
                motion.setY(-motion.getY());
                break;
            case EAST: case WEST:
                motion.setX(-motion.getX());
                break;
            case NORTH: case SOUTH:
                motion.setZ(-motion.getZ());
                break;
            default:
                break;
        }

        projectile.setMotion(motion);

        return true;
    }


    @Override
    public String getKeyword() {
        return "Bouncy";
    }

    @Override
    public Bouncy serialize(File file, ConfigurationSection configurationSection, String path) {
        ListHolder<Material> blocks = new ListHolder<Material>().serialize(file, configurationSection, path + ".Blocks", Material.class);
        ListHolder<EntityType> entities = new ListHolder<EntityType>().serialize(file, configurationSection, path + ".Entities", EntityType.class);

        if (blocks == null && entities == null) return null;

        int maximumBounceAmount = configurationSection.getInt(path + ".Maximum_Bounce_Amount", 1);

        return new Bouncy(maximumBounceAmount, blocks, entities);
    }
}