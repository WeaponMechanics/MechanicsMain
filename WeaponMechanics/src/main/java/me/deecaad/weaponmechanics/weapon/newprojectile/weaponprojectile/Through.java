package me.deecaad.weaponmechanics.weapon.newprojectile.weaponprojectile;

import me.deecaad.core.file.Serializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.io.File;

public class Through implements Serializer<Through> {

    private int maximumThroughAmount;
    private ListHolder<Material> blocks;
    private ListHolder<EntityType> entities;

    /**
     * Empty for serializers
     */
    public Through() { }

    public Through(int maximumThroughAmount, ListHolder<Material> blocks, ListHolder<EntityType> entities) {
        this.maximumThroughAmount = maximumThroughAmount;
        this.blocks = blocks;
        this.entities = entities;
    }

    /**
     * @param projectile the projectile
     * @param hit the hit entity or block
     * @return true if projectile went through, false if projectile should die
     */
    public boolean handleThrough(WeaponProjectile projectile, RayTraceResult hit) {

        Double speedModifier = hit.isBlock() ? blocks.isValid(hit.getBlock().getType()) : entities.isValid(hit.getLivingEntity().getType());

        // Speed modifier null would mean that it wasn't valid material or entity type
        if (speedModifier == null || maximumThroughAmount - projectile.getThroughAmount() < 0) {
            // Projectile should die
            return false;
        }

        if (speedModifier != 1.0) projectile.setMotion(projectile.getMotion().multiply(speedModifier));

        return true;
    }

    @Override
    public String getKeyword() {
        return "Through";
    }

    @Override
    public Through serialize(File file, ConfigurationSection configurationSection, String path) {
        ListHolder<Material> blocks = new ListHolder<Material>().serialize(file, configurationSection, path + ".Blocks", Material.class);
        ListHolder<EntityType> entities = new ListHolder<EntityType>().serialize(file, configurationSection, path + ".Entities", EntityType.class);

        if (blocks == null && entities == null) return null;

        int maximumThroughAmount = configurationSection.getInt(path + ".Maximum_Through_Amount", 1);

        return new Through(maximumThroughAmount, blocks, entities);
    }
}
