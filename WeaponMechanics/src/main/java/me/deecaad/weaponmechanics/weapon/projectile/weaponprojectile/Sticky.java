package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.file.Serializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.io.File;

public class Sticky implements Serializer<Sticky> {

    private ListHolder<Material> blocks;
    private ListHolder<EntityType> entities;

    /**
     * Empty for serializers
     */
    public Sticky() { }

    public Sticky(ListHolder<Material> blocks, ListHolder<EntityType> entities) {
        this.blocks = blocks;
        this.entities = entities;
    }

    public boolean handleSticking(WeaponProjectile projectile, RayTraceResult hit) {
        Double isValid = hit.isBlock() && blocks != null ? blocks.isValid(hit.getBlock().getType()) :
                entities != null ? entities.isValid(hit.getLivingEntity().getType()) : null;

        // Null means that it wasn't valid material or entity type
        if (isValid == null) {
            // Projectile should die
            return false;
        }

        projectile.setStickedData(new StickedData(hit));
        return true;
    }

    @Override
    public String getKeyword() {
        return "Sticky";
    }

    @Override
    public Sticky serialize(File file, ConfigurationSection configurationSection, String path) {

        ListHolder<Material> blocks = new ListHolder<Material>().serialize(file, configurationSection, path + ".Blocks", Material.class);
        ListHolder<EntityType> entities = new ListHolder<EntityType>().serialize(file, configurationSection, path + ".Entities", EntityType.class);

        if (blocks == null && entities == null) return null;

        return new Sticky(blocks, entities);
    }
}
