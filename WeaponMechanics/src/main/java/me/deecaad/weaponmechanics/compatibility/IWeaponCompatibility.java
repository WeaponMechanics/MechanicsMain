package me.deecaad.weaponmechanics.compatibility;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.VoxelShape;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface IWeaponCompatibility {

    /**
     * @return the scope compatibility
     */
    @Nonnull
    IScopeCompatibility getScopeCompatibility();

    /**
     * If entity is invulnerable or non alive this will always return null.
     * Otherwise this will always have non null value.
     *
     * @param entity the entity
     * @return the living entity's hit box
     */
    default HitBox getHitBox(Entity entity) {
        if (entity.isInvulnerable() || !entity.getType().isAlive() || entity.isDead()) return null;

        // This default should only be used after 1.13 R2

        BoundingBox boundingBox = entity.getBoundingBox();
        HitBox hitBox = new HitBox(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        hitBox.setLivingEntity((LivingEntity) entity);

        if (entity instanceof ComplexLivingEntity && WeaponMechanics.getBasicConfigurations().getBool("Check_Accurate_Hitboxes", true)) {
            for (ComplexEntityPart entityPart : ((ComplexLivingEntity) entity).getParts()) {
                BoundingBox boxPart = entityPart.getBoundingBox();
                hitBox.addVoxelShapePart(new HitBox(boxPart.getMinX(), boxPart.getMinY(), boxPart.getMinZ(), boxPart.getMaxX(), boxPart.getMaxY(), boxPart.getMaxZ()));
            }
        }

        return hitBox;
    }

    /**
     * If block is air, liquid or some other passable block (e.g. torch, flower)
     * then this method WILL always return null. Basically if this method returns null
     * means that block is passable.
     *
     * @param block the block
     * @return the block's hit box or null if its passable for example
     */
    default HitBox getHitBox(Block block) {

        // This default should only be used after 1.17
        if (block.isEmpty() || block.isLiquid() || block.isPassable()) return null;

        BoundingBox boundingBox = block.getBoundingBox();
        HitBox hitBox = new HitBox(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        hitBox.setBlockHitBox(block);

        // This default should only be used after 1.17 R1
        if (WeaponMechanics.getBasicConfigurations().getBool("Check_Accurate_Hitboxes", true)) {
            Collection<BoundingBox> voxelShape = block.getCollisionShape().getBoundingBoxes();
            if (voxelShape.size() > 1) {
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();
                for (BoundingBox boxPart : voxelShape) {
                    hitBox.addVoxelShapePart(new HitBox(x + boxPart.getMinX(), y + boxPart.getMinY(), z + boxPart.getMinZ(),
                            x + boxPart.getMaxX(), y + boxPart.getMaxY(), z + boxPart.getMaxZ()));
                }
            }
        }

        return hitBox;
    }

    /**
     * Used to get width of entity
     *
     * @param entity the entity whose width to get
     * @return the width of entity
     */
    default double getWidth(Entity entity) {
        // 1.12 ->
        // -> entity.getWidth
        // <- 1.11
        // -> nmsEntity.width
        return entity.getWidth();
    }

    /**
     * Used to get height of entity
     *
     * @param entity the entity whose height to get
     * @return the height of entity
     */
    default double getHeight(Entity entity) {
        // 1.12 ->
        // -> entity.getHeight
        // <- 1.11
        // -> nmsEntity.height
        return entity.getHeight();
    }

    /**
     * Rotates player's camera rotation with given values.
     * Absolute true means that yaw and pitch will be SET to the given values.
     * While as absolute false means that yaw and pitch is ADDED to the given values.
     *
     * Having absolute true may cause that player's movement glitches a bit.
     *
     * @param player the player whose camera rotation to rotate
     * @param yaw absolute or relative rotation on the X axis, in degrees
     * @param pitch absolute or relative rotation on the Y axis, in degrees
     * @param absolute whether to use absolute rotation
     */
    void modifyCameraRotation(Player player, float yaw, float pitch, boolean absolute);

    /**
     * Logs "fake" damage to the given <code>victim</code>'s <code>CombatTracker</code>. This is important
     * for death messages, and any plugins that may use minecraft's built in combat tracker.
     *
     * @param victim The entity receiving the damage
     * @param source The entity giving the damage
     * @param health The health of the entity
     * @param damage The damage being applied to the entity
     * @param isMelee Whether or not this is a melee attack (And not a projectile)
     */
    void logDamage(LivingEntity victim, LivingEntity source, double health, double damage, boolean isMelee);

    /**
     * Sets which player killed the <code>victim</code>. Entities that are killed by players
     * will drop their experience.
     *
     * @param victim The entity that died
     * @param killer The killer
     */
    void setKiller(LivingEntity victim, Player killer);
}