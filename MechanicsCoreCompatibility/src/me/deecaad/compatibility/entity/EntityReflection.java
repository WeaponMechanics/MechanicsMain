package me.deecaad.compatibility.entity;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

import static me.deecaad.core.MechanicsCore.debug;
import static me.deecaad.core.utils.ReflectionUtil.*;

public class EntityReflection implements EntityCompatibility {

    private static final Class<?> nmsEntityClass;
    private static final Class<?> cbEntityClass;
    private static final Class<?> dataWatcherClass;

    private static final Method getId;
    private static final Method getHandle;
    private static final Method getDataWatcher;

    private static final Constructor<?> spawnPacketConstructor;
    private static final Constructor<?> metadataPacketConstructor;
    private static final Constructor<?> destroyPacketConstructor;

    static {
        nmsEntityClass = getNMSClass("Entity");
        cbEntityClass = getCBClass("entity.CraftEntity");
        dataWatcherClass = getNMSClass("DataWatcher");

        getId = getMethod(nmsEntityClass, "getId");
        getHandle = getMethod(cbEntityClass, "getHandle");
        getDataWatcher = getMethod(nmsEntityClass, "getDataWatcher");

        spawnPacketConstructor = getConstructor(getNMSClass("PacketPlayOutSpawnEntity"), nmsEntityClass);
        metadataPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityMetadata"), int.class, dataWatcherClass, boolean.class);
        destroyPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityDestroy"), int[].class);
    }

    @Override
    public Object getNMSEntity(Entity entity) {
        return invokeMethod(getHandle, entity);
    }

    @Override
    public int getId(Entity entity) {
        return (int) invokeMethod(getId, getNMSEntity(entity));
    }

    @Override
    public Object getSpawnPacket(Object entity) {
        if (!nmsEntityClass.isInstance(entity)) {
            debug.error("Object must be NMS entity! Got: " + entity);
            return null;
        }

        return newInstance(spawnPacketConstructor, entity);
    }

    @Override
    public Object getVelocityPacket(Object entity, Vector velocity) {
        return null;
    }

    @Override
    public Object getMetadataPacket(Object entity) {
        if (!nmsEntityClass.isInstance(entity)) {
            debug.error("Object must be NMS entity! Got: " + entity);
            return null;
        }

        Object id = invokeMethod(getId, entity);
        Object dataWatcher = invokeMethod(getDataWatcher, entity);

        return newInstance(metadataPacketConstructor, id, dataWatcher, true);
    }

    @Override
    public Object getMetadataPacket(Object entity, boolean isEnableFlags, EntityMeta... flags) {
        if (!nmsEntityClass.isInstance(entity)) {
            debug.error("Object must be NMS entity! Got: " + entity);
            return null;
        }

        return null; // todo
    }

    @Override
    public Object setMetadata(Object packet, boolean isEnableFlags, EntityMeta... flags) {
        return null; // todo
    }

    @Override
    public Object getDestroyPacket(Object entity) {
        return newInstance(destroyPacketConstructor, invokeMethod(getId, entity));
    }

    @Override
    public void spawnFirework(Location loc, Collection<? extends Player> players, byte flightTime, FireworkEffect... effects) {

    }

    @Override
    public Object getGoalSelector(CustomPathfinderGoal goal) {
        throw new UnsupportedOperationException("Cannot reflectively make sub-classes for this version!");
    }
}