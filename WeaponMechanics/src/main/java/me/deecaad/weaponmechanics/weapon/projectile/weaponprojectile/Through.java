package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.utils.ray.RayTraceResult;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;

public class Through implements Serializer<Through>, Cloneable {

    // -1 = infinite
    private int maximumThroughAmount;

    private ListHolder<Material> blocks;
    private ListHolder<EntityType> entities;

    /**
     * Default constructor for serializer
     */
    public Through() {
    }

    public Through(int maximumThroughAmount, ListHolder<Material> blocks, ListHolder<EntityType> entities) {
        this.maximumThroughAmount = maximumThroughAmount;
        this.blocks = blocks;
        this.entities = entities;
    }

    public int getMaximumThroughAmount() {
        return maximumThroughAmount;
    }

    public void setMaximumThroughAmount(int maximumThroughAmount) {
        this.maximumThroughAmount = maximumThroughAmount;
    }

    /**
     * @param projectile the projectile
     * @param hit the hit entity or block
     * @return true if projectile went through, false if projectile should die
     */
    public boolean handleThrough(WeaponProjectile projectile, RayTraceResult hit) {

        Double speedModifier;
        if (hit.isBlock()) {
            speedModifier = blocks != null ? blocks.isValid(hit.getBlock().getType()) : null;
        } else {
            speedModifier = entities != null ? entities.isValid(hit.getLivingEntity().getType()) : null;
        }

        // Speed modifier null would mean that it wasn't valid material or entity type
        if (speedModifier == null || (maximumThroughAmount != -1 && maximumThroughAmount - projectile.getThroughAmount() < 1)) {
            // Projectile should die
            return false;
        }

        if (speedModifier != 1.0) projectile.setMotion(projectile.getMotion().multiply(speedModifier));

        return true;
    }

    public boolean quickValidCheck(Material material) {
        return blocks != null && blocks.isValid(material) != null;
    }

    @Override
    public String getKeyword() {
        return "Through";
    }

    @Override
    @Nonnull
    public Through serialize(SerializeData data) throws SerializerException {
        ListHolder<Material> blocks = data.of("Blocks").serialize(new ListHolder<>(Material.class));
        ListHolder<EntityType> entities = data.of("Entities").serialize(new ListHolder<>(EntityType.class));

        if (blocks == null && entities == null) {
            throw data.exception(null, "'Through' requires at least one of 'Blocks' or 'Entities'");
        }

        int maximumThroughAmount = data.of("Maximum_Through_Amount").getInt(1);

        return new Through(maximumThroughAmount, blocks, entities);
    }

    @Override
    public Through clone() {
        try {
            return (Through) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
