package me.deecaad.compatibility.shoot;

import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class Shoot_1_11_R1 implements IShootCompatibility {

    @Override
    public double getWidth(Entity entity) {
        return ((CraftEntity) entity).getHandle().width;
    }

    @Override
    public double getHeight(Entity entity) {
        return ((CraftEntity) entity).getHandle().length;
    }
}