package me.deecaad.compatibility.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static me.deecaad.core.utils.ReflectionUtil.*;

public class EntityReflection implements EntityCompatibility {

    private static final Class<?> entityClass;
    private static final Class<?> dataWatcherClass;
    private static final Method getId;
    private static final Method getDataWatcher;


    private static final Constructor<?> spawnPacketConstructor;
    private static final Constructor<?> metadataPacketConstructor;
    private static final Constructor<?> destroyPacketConstructor;

    static {
        entityClass = getNMSClass("Entity");
        dataWatcherClass = getNMSClass("DataWatcher");
        getId = getMethod(entityClass, "getId");
        getDataWatcher = getMethod(entityClass, "getDataWatcher");

        spawnPacketConstructor = getConstructor(getNMSClass("PacketPlayOutSpawnEntity"), entityClass);
        metadataPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityMetadata"), int.class, dataWatcherClass, boolean.class);
        destroyPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityDestroy"), int.class);
    }

    @Override
    public Object getSpawnPacket(Object entity) {
        return newInstance(spawnPacketConstructor, entity);
    }

    @Override
    public Object getMetadataPacket(Object entity) {
        Object id = invokeMethod(getId, entity);
        Object dataWatcher = invokeMethod(getDataWatcher, entity);

        return newInstance(metadataPacketConstructor, id, dataWatcher, true);
    }

    @Override
    public Object getDestroyPacket(Object entity) {
        return newInstance(destroyPacketConstructor, invokeMethod(getId, entity));
    }
}
