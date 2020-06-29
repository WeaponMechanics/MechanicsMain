package me.deecaad.compatibility.item.dropped;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static me.deecaad.core.utils.ReflectionUtil.*;

public class DropReflection implements DropCompatibility {

    private static final Method getHandle;
    private static final Method asNMSCopy;

    private static final Constructor<?> entityItemConstructor;

    static {

        getHandle = getMethod(getCBClass("CraftWorld"), "getHandle");
        asNMSCopy = getMethod(getCBClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);

        entityItemConstructor = getConstructor(getNMSClass("EntityItem"), getHandle.getReturnType().getSuperclass(), double.class, double.class, double.class, asNMSCopy.getReturnType());
    }

    @Override
    public Object toNMSItemEntity(ItemStack item, World world, double x, double y, double z) {
        Object nmsWorld = invokeMethod(getHandle, world);
        Object nmsItem = invokeMethod(asNMSCopy, null, item);

        return newInstance(entityItemConstructor, nmsWorld, x, y, z, nmsItem);
    }
}
