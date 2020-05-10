package me.deecaad.weaponcompatibility.shoot;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface IShootCompatibility {

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
}