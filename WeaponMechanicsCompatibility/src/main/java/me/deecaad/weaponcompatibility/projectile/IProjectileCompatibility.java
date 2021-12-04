package me.deecaad.weaponcompatibility.projectile;

import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public interface IProjectileCompatibility {

    /**
     * Spawns projectile disguise for all players within 150 blocks.
     * This is only ran if disguises are used.
     *
     * @param customProjectile the projectile used to fetch all required data
     * @param location the location vector of projectile
     * @param motion the motion vector projectile
     */
    void spawnDisguise(CustomProjectile customProjectile, Vector location, Vector motion);

    /**
     * Updates projectile velocity and location for all players within 90 blocks.
     * This is only ran if disguises are used.
     *
     * @param customProjectile the projectile used to fetch all required data
     * @param location the location vector of projectile
     * @param motion the motion vector projectile
     * @param lastLocation the last location vector of projectile
     */
    void updateDisguise(CustomProjectile customProjectile, Vector location, Vector motion, Vector lastLocation);

    /**
     * Destroys disguise from all players within 150 blocks.
     * This is only ran if disguises are used.
     *
     * @param customProjectile the projectile used to fetch all required data
     */
    void destroyDisguise(CustomProjectile customProjectile);

    /**
     * Get DEFAULT entity width and height.
     * This will return double array where 0 is width and 1 is height.
     *
     * @param entityType the entity type which width and height to get
     * @return the default width[0] and height[1] of entity type
     */
    double[] getDefaultWidthAndHeight(EntityType entityType);

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
        return new HitBox(boundingBox.getMin(), boundingBox.getMax());
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
        return new HitBox(boundingBox.getMin(), boundingBox.getMax());
    }

    /**
     * @param customProjectile the projectile used to fetch all required data
     * @param degrees the pitch degrees to convert to byte
     * @return the byte value of pitch
     */
    default byte convertPitchToByte(CustomProjectile customProjectile, float degrees) {
        byte byteValue = convertDegreesToByte(degrees);
        EntityType type = customProjectile.projectile.getProjectileDisguise();
        if (!type.isAlive() || type == EntityType.WITHER_SKULL) {
            return (byte) -byteValue;
        }
        return byteValue;
    }

    /**
     * @param customProjectile the projectile used to fetch all required data
     * @param degrees the yaw degrees to convert to byte
     * @return the byte value of yaw
     */
    default byte convertYawToByte(CustomProjectile customProjectile, float degrees) {
        byte byteValue = convertDegreesToByte(degrees);
        EntityType type = customProjectile.projectile.getProjectileDisguise();
        switch (type) {
            case ARROW:
                return (byte) -byteValue;
            case WITHER_SKULL:
            case ENDER_DRAGON:
                return (byte) (byteValue - 128);
            default:
                if (!type.isAlive() && type != EntityType.ARMOR_STAND) {
                    return (byte) (byteValue - 64);
                }
                return byteValue;
        }
    }

    /**
     * @param degrees the degrees to convert to byte
     * @return the degrees as byte
     */
    default byte convertDegreesToByte(float degrees) {
        return (byte) (degrees * 256.0F / 360.0F);
    }

    /**
     * Simple flooring method used in NMS
     *
     * @param toFloor value to be floored
     * @return the floored value
     */
    default int floor(double toFloor) {
        int flooredValue = (int) toFloor;
        return toFloor < (double) flooredValue ? flooredValue - 1 : flooredValue;
    }
}