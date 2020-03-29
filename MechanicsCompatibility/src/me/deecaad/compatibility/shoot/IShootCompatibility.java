package me.deecaad.compatibility.shoot;

import org.bukkit.entity.Entity;

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

    default double getHeight(Entity entity) {
        // 1.12 ->
        // -> entity.getHeight
        // <- 1.11
        // -> nmsEntity.height
        return entity.getHeight();
    }
}