package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.utils.ray.RayTraceResult;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;

public class Sticky implements Serializer<Sticky>, Cloneable {

    private ListHolder<Material> blocks;
    private ListHolder<EntityType> entities;

    /**
     * Default constructor for serializer
     */
    public Sticky() {
    }

    public Sticky(ListHolder<Material> blocks, ListHolder<EntityType> entities) {
        this.blocks = blocks;
        this.entities = entities;
    }

    public boolean handleSticking(WeaponProjectile projectile, RayTraceResult hit) {
        Double isValid;
        if (hit.isBlock()) {
            isValid = blocks != null ? blocks.isValid(hit.getBlock().getType()) : null;
        } else {
            isValid = entities != null ? entities.isValid(hit.getLivingEntity().getType()) : null;
        }

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
    @Nonnull
    public Sticky serialize(SerializeData data) throws SerializerException {
        ListHolder<Material> blocks = data.of("Blocks").serialize(new ListHolder<>(Material.class));
        ListHolder<EntityType> entities = data.of("Entities").serialize(new ListHolder<>(EntityType.class));

        if (blocks == null && entities == null) {
            throw data.exception(null, "'Sticky' requires at least one of 'Blocks' or 'Entities'");
        }

        return new Sticky(blocks, entities);
    }

    @Override
    public Sticky clone() {
        try {
            return (Sticky) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
