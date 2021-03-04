package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.file.Serializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Set;

public class Through implements Serializer<Through> {

    private int blockMaximumPassThroughs;
    private int entityMaximumPassThroughs;
    private ProjectileListData<Material> blocks;
    private ProjectileListData<EntityType> entities;

    /**
     * Empty constructor to be used as serializer
     */
    public Through() { }

    public Through(int blockMaximumPassThroughs, int entityMaximumPassThroughs, ProjectileListData<Material> blocks, ProjectileListData<EntityType> entities) {
        this.blockMaximumPassThroughs = blockMaximumPassThroughs;
        this.entityMaximumPassThroughs = entityMaximumPassThroughs;
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
     * @return true if projectile went through
     */
    public boolean handleThrough(Collisions collisions, CollisionData collision, Vector motion) {
        Double speedModifier;
        Set<CollisionData> typeCollisions;
        int maximumPassThroughs;

        Block bukkitBlock = collision.getBlock();
        if (bukkitBlock != null) {
            speedModifier = blocks.isValid(bukkitBlock.getType());
            maximumPassThroughs = blockMaximumPassThroughs;
            typeCollisions = collisions.getBlockCollisions();
        } else {
            speedModifier = entities.isValid(collision.getLivingEntity().getType());
            maximumPassThroughs = entityMaximumPassThroughs;
            typeCollisions = collisions.getEntityCollisions();
        }

        // Speed modifier null would mean that it wasn't valid material or entity type
        if (speedModifier == null || maximumPassThroughs - typeCollisions.size() < 0) { // Projectile should die
            typeCollisions.add(collision);
            return false;
        }

        // Only modify motion if it's not 1.0
        if (speedModifier != 1.0) {
            motion.multiply(speedModifier);
        }

        typeCollisions.add(collision);

        return true;
    }

    @Override
    public String getKeyword() {
        return "Through";
    }

    @Override
    public Through serialize(File file, ConfigurationSection configurationSection, String path) {
        ProjectileListData<Material> blocks = new ProjectileListData<Material>().serialize(Material.class, file, configurationSection, path + ".Blocks");
        ProjectileListData<EntityType> entities = new ProjectileListData<EntityType>().serialize(EntityType.class, file, configurationSection, path + ".Entities");

        if (blocks == null && entities == null) {
            return null;
        }

        int blockMaximumPassThroughs = configurationSection.getInt(path + ".Blocks.Maximum_Pass_Throughs", 2);
        int entityMaximumPassThroughs = configurationSection.getInt(path + ".Entities.Maximum_Pass_Throughs", 2);

        return new Through(blockMaximumPassThroughs, entityMaximumPassThroughs, blocks, entities);
    }
}