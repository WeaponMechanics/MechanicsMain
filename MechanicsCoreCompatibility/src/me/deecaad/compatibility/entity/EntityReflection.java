package me.deecaad.compatibility.entity;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    private static final Method getItems;
    private static final Method methodE;
    private static final Method worldGetHandle;
    private static final Method asNMSCopy;

    private static final Constructor<?> spawnPacketConstructor;
    private static final Constructor<?> metadataPacketConstructor;
    private static final Constructor<?> destroyPacketConstructor;
    private static final Constructor<?> entityItemConstructor;

    static {
        nmsEntityClass = getNMSClass("Entity");
        cbEntityClass = getCBClass("entity.CraftEntity");
        dataWatcherClass = getNMSClass("DataWatcher");

        getId = getMethod(nmsEntityClass, "getId");
        getHandle = getMethod(cbEntityClass, "getHandle");
        getDataWatcher = getMethod(nmsEntityClass, "getDataWatcher");
        getItems = getMethod(dataWatcherClass, "c");
        methodE = getMethod(dataWatcherClass, "e");
        //getData = getMethod(m)
        worldGetHandle = getMethod(getCBClass("CraftWorld"), "getHandle");
        asNMSCopy = getMethod(getCBClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);

        spawnPacketConstructor = getConstructor(getNMSClass("PacketPlayOutSpawnEntity"), nmsEntityClass);
        metadataPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityMetadata"), int.class, dataWatcherClass, boolean.class);
        destroyPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityDestroy"), int[].class);
        entityItemConstructor = getConstructor(getNMSClass("EntityItem"), getNMSClass("World"), double.class, double.class, double.class, asNMSCopy.getReturnType());
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

        // Setup the byte data
        byte mask = 0;
        for (EntityMeta flag : flags) {
            mask |= flag.getMask();
        }

        return null;
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

    @Override
    public Object toNMSItemEntity(ItemStack item, World world, double x, double y, double z) {
        Object nmsWorld = invokeMethod(getHandle, world);
        Object nmsItem = invokeMethod(asNMSCopy, null, item);

        return newInstance(entityItemConstructor, nmsWorld, x, y, z, nmsItem);
    }
}
