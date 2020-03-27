package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.versions.ICompatibility;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;

public interface IProjectileCompatibility {

    /**
     * Spawns projectile disguise for all players within 150 blocks.
     * This is only ran if disguises are used.
     *
     * @param customProjectile the projectile used to fetch all required data
     */
    void spawnDisguise(CustomProjectile customProjectile);

    /**
     * Updates projectile velocity and location for all players within 90 blocks.
     * This is only ran if disguises are used.
     *
     * @param customProjectile the projectile used to fetch all required data
     * @param length the motion length
     */
    void updateDisguise(CustomProjectile customProjectile, float length);

    /**
     * Destroys disguise from all players within 150 blocks.
     * This is only ran if disguises are used.
     *
     * @param customProjectile the projectile used to fetch all required data
     */
    void destroyDisguise(CustomProjectile customProjectile);

    /**
     * If entity is invulnerable this will always return null.
     * Otherwise this will always have non null value.
     *
     * @param entity the entity
     * @return the living entity's hit box
     */
    default HitBox getHitBox(Entity entity) {
        if (entity.isInvulnerable()) return null;

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
     * Player has to be within distance to receive packet.
     * This does not use Y axis.
     *
     * 8050 = 90 blocks.
     * 22500 = 150 blocks.
     *
     * @param customProjectile the projectile used to fetch all required data
     * @param distance the distance squared XZ
     * @param packets the packet to send
     */
    default void sendUpdatePackets(CustomProjectile customProjectile, int distance, Object... packets) {
        ICompatibility compatibility = CompatibilityAPI.getCompatibility();
        double x = customProjectile.location.getX();
        double z = customProjectile.location.getZ();
        for (Player player : customProjectile.world.getPlayers()) {
            Location playerLocation = player.getLocation();

            // 22500 = around 150 blocks
            if (NumberConversions.square(x - playerLocation.getX()) + NumberConversions.square(z - playerLocation.getZ()) < distance) {
                compatibility.sendPackets(player, packets);
            }
        }
    }

    /**
     * Calculates new yaw and pitch based on the projectile motion.
     *
     * @param customProjectile the projectile used to fetch all required data
     */
    default void calculateYawAndPitch(CustomProjectile customProjectile) {
        double x = customProjectile.motion.getX();
        double z = customProjectile.motion.getZ();

        double PIx2 = 6.283185307179;
        customProjectile.yaw = (float) Math.toDegrees((Math.atan2(-x, z) + PIx2) % PIx2);
        customProjectile.yaw %= 360.0F;
        if (customProjectile.yaw >= 180.0F) {
            customProjectile.yaw -= 360.0F;
        } else if (customProjectile.yaw < -180.0F) {
            customProjectile.yaw += 360.0F;
        }

        customProjectile.pitch = (float) Math.toDegrees(Math.atan(-customProjectile.motion.getY() / Math.sqrt(NumberConversions.square(x) + NumberConversions.square(z))));
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