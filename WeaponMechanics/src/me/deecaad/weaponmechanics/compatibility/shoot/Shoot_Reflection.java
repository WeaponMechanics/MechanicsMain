package me.deecaad.weaponmechanics.compatibility.shoot;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Shoot_Reflection implements IShootCompatibility {

    private Method entityGetHandle;
    private Field entityWidth;

    public Shoot_Reflection() {
        this.entityGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("entity.CraftEntity"), "getHandle");
        this.entityWidth = ReflectionUtil.getField(ReflectionUtil.getNMSClass("Entity"), "width");
    }

    @Override
    public double getWidth(Entity entity) {
        if (CompatibilityAPI.getVersion() >= 1.12) {
            return entity.getWidth();
        }
        Object nmsEntity = ReflectionUtil.invokeMethod(entityGetHandle, entity);
        return (float) ReflectionUtil.invokeField(entityWidth, nmsEntity);
    }
}