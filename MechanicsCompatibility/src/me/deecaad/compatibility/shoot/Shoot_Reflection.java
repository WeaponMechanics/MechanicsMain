package me.deecaad.compatibility.shoot;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Shoot_Reflection implements IShootCompatibility {

    private Method entityGetHandle;
    private Field entityWidth;
    private Field entityHeight;

    public Shoot_Reflection() {
        this.entityGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("entity.CraftEntity"), "getHandle");
        this.entityWidth = ReflectionUtil.getField(ReflectionUtil.getNMSClass("Entity"), "width");
        this.entityHeight = ReflectionUtil.getField(ReflectionUtil.getNMSClass("Entity"), "length");
    }

    @Override
    public double getWidth(Entity entity) {
        if (CompatibilityAPI.getVersion() >= 1.12) {
            return entity.getWidth();
        }
        Object nmsEntity = ReflectionUtil.invokeMethod(entityGetHandle, entity);
        return (float) ReflectionUtil.invokeField(entityWidth, nmsEntity);
    }

    @Override
    public double getHeight(Entity entity) {
        if (CompatibilityAPI.getVersion() >= 1.12) {
            return entity.getHeight();
        }
        Object nmsEntity = ReflectionUtil.invokeMethod(entityGetHandle, entity);
        return (float) ReflectionUtil.invokeField(entityHeight, nmsEntity);
    }
}