package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.file.Serializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Set;

public class Bouncy implements Serializer<Bouncy> {

    public static final double REQUIRED_MOTION_TO_BOUNCE = 0.3;

    private int blockMaximumBounceAmount;
    private int entityMaximumBounceAmount;
    private ProjectileListData<Material> blocks;
    private ProjectileListData<EntityType> entities;

    /**
     * Empty for serializers
     */
    public Bouncy() { }

    public Bouncy(int blockMaximumBounceAmount, int entityMaximumBounceAmount, ProjectileListData<Material> blocks, ProjectileListData<EntityType> entities) {
        this.blockMaximumBounceAmount = blockMaximumBounceAmount;
        this.entityMaximumBounceAmount = entityMaximumBounceAmount;
        this.blocks = blocks;
        this.entities = entities;
    }

    public boolean hasBlocks() {
        return blocks != null;
    }

    public boolean hasEntities() {
        return entities != null;
    }

    /**
     * @return true if projectile bounced
     */
    public boolean handleBounce(CustomProjectile projectile, Collisions collisions, CollisionData collision, Vector motion) {
        if (projectile.getMotionLength() < REQUIRED_MOTION_TO_BOUNCE) {
            return false;
        }

        Double speedModifier;
        Set<CollisionData> typeCollisions;
        int maximumBounceAmount;

        Block bukkitBlock = collision.getBlock();
        if (bukkitBlock != null) {
            speedModifier = blocks.isValid(bukkitBlock.getType());
            maximumBounceAmount = blockMaximumBounceAmount;
            typeCollisions = collisions.getBlockCollisions();
        } else {
            speedModifier = entities.isValid(collision.getLivingEntity().getType());
            maximumBounceAmount = entityMaximumBounceAmount;
            typeCollisions = collisions.getEntityCollisions();
        }

        // Speed modifier null would mean that it wasn't valid material or entity type
        if (speedModifier == null || maximumBounceAmount - typeCollisions.size() < 0) { // Projectile should die
            typeCollisions.add(collision);
            return false;
        }

        // Only modify motion if it's not 1.0
        if (speedModifier != 1.0) {
            motion.multiply(speedModifier);
        }

        BlockFace hitFace = collision.getBlockFace();
        if (hitFace != null) {

            switch (hitFace) {
                case UP: case DOWN:
                    motion.setY(-motion.getY());
                    break;
                case EAST: case WEST:
                    motion.setX(-motion.getX());
                    break;
                case NORTH: case SOUTH:
                    motion.setZ(-motion.getZ());
            }
        }

        typeCollisions.add(collision);

        return true;
    }

    @Override
    public String getKeyword() {
        return "Bouncy";
    }

    @Override
    public Bouncy serialize(File file, ConfigurationSection configurationSection, String path) {

        ProjectileListData<Material> blocks = new ProjectileListData<Material>().serialize(Material.class, file, configurationSection, path + ".Blocks");
        ProjectileListData<EntityType> entities = new ProjectileListData<EntityType>().serialize(EntityType.class, file, configurationSection, path + ".Entities");

        if (blocks == null && entities == null) {
            return null;
        }

        int blockMaximumBounceAmount = configurationSection.getInt(path + ".Blocks.Maximum_Bounce_Amount", 2);
        int entityMaximumBounceAmount = configurationSection.getInt(path + ".Entities.Maximum_Bounce_Amount", 2);

        return new Bouncy(blockMaximumBounceAmount, entityMaximumBounceAmount, blocks, entities);
    }
}