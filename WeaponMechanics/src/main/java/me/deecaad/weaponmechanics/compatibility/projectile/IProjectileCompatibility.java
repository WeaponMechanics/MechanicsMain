package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public interface IProjectileCompatibility {

    /**
     * Spawns projectile disguise for all players within view distance.
     * Keeps updating projectile velocity and location for all players within view distance.
     * Destroys disguise if projectile gets marked for removal.
     *
     * @param projectile the projectile used for handling disguise
     */
    void disguise(AProjectile projectile);

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

        // This default should only be used after 1.13 R2

        if (block.isEmpty() || block.isLiquid() || block.isPassable()) return null;
        BoundingBox boundingBox = block.getBoundingBox();
        HitBox hitBox = new HitBox(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        hitBox.setBlockHitBox(block);
        return hitBox;
    }

    /**
     * @param entityType the entity type
     * @param degrees the pitch degrees to convert to byte
     * @return the byte value of pitch
     */
    default byte convertPitchToByte(EntityType entityType, float degrees) {
        byte byteValue = convertDegreesToByte(degrees);
        if (!entityType.isAlive() && entityType != EntityType.WITHER_SKULL) {
            return (byte) -byteValue;
        }
        return byteValue;
    }

    /**
     * @param entityType the entity type
     * @param degrees the yaw degrees to convert to byte
     * @return the byte value of yaw
     */
    default byte convertYawToByte(EntityType entityType, float degrees) {
        byte byteValue = convertDegreesToByte(degrees);
        if (CompatibilityAPI.getVersion() >= 1.09 && entityType == EntityType.SPECTRAL_ARROW) {
            return (byte) -byteValue;
        }
        switch (entityType) {
            case ARROW:
                return (byte) -byteValue;
            case WITHER_SKULL:
                return (byte) (byteValue - 128);
            default:
                if (!entityType.isAlive() && entityType != EntityType.ARMOR_STAND) {
                    return (byte) (byteValue - 64);
                }
                return byteValue;
        }
    }

    /**
     * @param normalizedMotion the normalized motion
     * @return the yaw in degrees
     */
    default float calculateYaw(Vector normalizedMotion) {
        double PI_2 = VectorUtil.PI_2;
        return (float) Math.toDegrees((Math.atan2(-normalizedMotion.getX(), normalizedMotion.getZ()) + PI_2) % PI_2);
    }

    /**
     * @param normalizedMotion the normalized motion
     * @return the pitch in degrees
     */
    default float calculatePitch(Vector normalizedMotion) {
        return (float) Math.toDegrees(Math.atan(-normalizedMotion.getY() / Math.sqrt(NumberConversions.square(normalizedMotion.getX()) + NumberConversions.square(normalizedMotion.getZ()))));
    }

    /**
     * @param degrees the degrees to convert to byte
     * @return the degrees as byte
     */
    default byte convertDegreesToByte(float degrees) {
        return (byte) (degrees * 256.0F / 360.0F);
    }
}