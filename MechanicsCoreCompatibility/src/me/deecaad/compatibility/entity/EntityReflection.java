package me.deecaad.compatibility.entity;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static me.deecaad.core.MechanicsCore.debug;
import static me.deecaad.core.utils.ReflectionUtil.*;

public class EntityReflection implements EntityCompatibility {

    private static final Class<?> nmsEntityClass;
    private static final Class<?> cbEntityClass;
    private static final Class<?> dataWatcherClass;
    private static final Class<?> entityFallingBlockClass;

    private static final Field metadataPacketID;
    private static final Field metadataPacketWatcher;
    private static final Field defaultItemField;
    private static final Field expectedLifeSpanField;

    private static final Method getId;
    private static final Method getHandle;
    private static final Method getDataWatcher;
    private static final Method getItems;
    private static final Method methodE;
    private static final Method getMetaValue;
    private static final Method setMetaValue;
    private static final Method worldGetHandle;
    private static final Method asNMSCopy;
    private static final Method getBlock;
    private static final Method blockGetCombinedId;
    private static final Method craftMagicNumbersGetItem;
    private static final Method getCraftItemFactoryInstance;
    private static final Method getItemMeta;
    private static final Method setItemMeta;
    private static final Method setDataWatcherObject;
    private static final Method getNMSBlockData;
    private static final Method getNMSBlockState;

    private static final Constructor<?> vectorConstructor;
    private static final Constructor<?> spawnPacketConstructor;
    private static final Constructor<?> spawnPacketBlockConstructor;
    private static final Constructor<?> metadataPacketConstructor;
    private static final Constructor<?> metadataPacketConstructor1;
    private static final Constructor<?> velocityPacketConstructor;
    private static final Constructor<?> destroyPacketConstructor;
    private static final Constructor<?> entityFireworksConstructor;
    private static final Constructor<?> nmsItemConstructor;
    private static final Constructor<?> entityItemConstructor;
    private static final Constructor<?> entityStatusPacketConstructor;
    private static final Constructor<?> entityFallingBlockConstructor;

    private static final Object fireworkItemDataWatcher;

    static {
        Class<?> worldClass = getNMSClass("World");

        nmsEntityClass = getNMSClass("Entity");
        cbEntityClass = getCBClass("entity.CraftEntity");
        dataWatcherClass = getNMSClass("DataWatcher");
        entityFallingBlockClass = getNMSClass("EntityFallingBlock");

        metadataPacketID = getField(getNMSClass("PacketPlayOutEntityMetadata"), null, int.class);
        metadataPacketWatcher = getField(getNMSClass("PacketPlayOutEntityMetadata"), null, List.class);
        defaultItemField = getField(getNMSClass("ItemStack"), null, getNMSClass("ItemStack"));
        expectedLifeSpanField = getField(getNMSClass("EntityFireworks"), "expectedLifespan");

        getId = getMethod(nmsEntityClass, "getId");
        getHandle = getMethod(cbEntityClass, "getHandle");
        getDataWatcher = getMethod(nmsEntityClass, "getDataWatcher");
        getItems = getMethod(getDataWatcher.getReturnType(), List.class, 1);
        methodE = getMethod(dataWatcherClass, void.class, 1);
        getMetaValue = getMethod(getNMSClass("DataWatcher$Item"), Object.class, 1);
        setMetaValue = getMethod(getNMSClass("DataWatcher$Item"), void.class, Object.class);
        worldGetHandle = getMethod(getCBClass("CraftWorld"), "getHandle");
        asNMSCopy = getMethod(getCBClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);
        getBlock = getMethod(entityFallingBlockClass, getNMSClass("IBlockData"));
        blockGetCombinedId = getMethod(getNMSClass("Block"), "getCombinedId", getBlock.getReturnType());
        craftMagicNumbersGetItem = getMethod(getCBClass("util.CraftMagicNumbers"), "getItem", Material.class);
        getCraftItemFactoryInstance = getMethod(getCBClass("inventory.CraftItemFactory"), "instance");
        getItemMeta = getMethod(getCraftItemFactoryInstance.getReturnType(), "getItemMeta", Material.class);
        setItemMeta = getMethod(getCBClass("inventory.CraftItemStack"), "setItemMeta", getNMSClass("ItemStack"), ItemMeta.class);
        setDataWatcherObject = getMethod(dataWatcherClass, "set", getNMSClass("DataWatcherObject"), Object.class);
        getNMSBlockData = getMethod(getCBClass("block.data.CraftBlockData"), "getState");
        getNMSBlockState = getMethod(getCBClass("block.CraftBlockState"), "getHandle");

        vectorConstructor = getConstructor(getNMSClass("Vec3D"), double.class, double.class, double.class);
        spawnPacketConstructor = getConstructor(getNMSClass("PacketPlayOutSpawnEntity"), nmsEntityClass);
        spawnPacketBlockConstructor = getConstructor(getNMSClass("PacketPlayOutSpawnEntity"), nmsEntityClass, blockGetCombinedId.getReturnType());
        metadataPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityMetadata"), int.class, dataWatcherClass, boolean.class);
        metadataPacketConstructor1 = getConstructor(getNMSClass("PacketPlayOutEntityMetadata"));
        velocityPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityVelocity"), int.class, vectorConstructor.getDeclaringClass());
        destroyPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityDestroy"), int[].class);
        entityFireworksConstructor = getConstructor(getNMSClass("EntityFireworks"), worldClass, double.class, double.class, double.class, getNMSClass("ItemStack"));
        nmsItemConstructor = getConstructor(getNMSClass("ItemStack"), getNMSClass("IMaterial"));
        entityItemConstructor = getConstructor(getNMSClass("EntityItem"), getNMSClass("World"), double.class, double.class, double.class, asNMSCopy.getReturnType());
        entityStatusPacketConstructor = getConstructor(getNMSClass("PacketPlayOutEntityStatus"), nmsEntityClass, byte.class);
        entityFallingBlockConstructor = getConstructor(entityFallingBlockClass, worldClass, double.class, double.class, double.class, getNMSBlockData.getReturnType());

        fireworkItemDataWatcher = invokeField(getField(getNMSClass("EntityFireworks"), "FIREWORK_ITEM"), null);
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
        } else if (entityFallingBlockClass.isInstance(entity)) {
            Object blockData = invokeMethod(getBlock, entity);
            Object combinedId = invokeMethod(blockGetCombinedId, blockData);
            return newInstance(spawnPacketBlockConstructor, entity, combinedId);
        } else {
            return newInstance(spawnPacketConstructor, entity);
        }
    }

    @Override
    public Object getVelocityPacket(Object entity, Vector velocity) {
        if (!nmsEntityClass.isInstance(entity)) {
            debug.error("Object must be NMS entity! Got: " + entity);
            return null;
        }

        Object vector = newInstance(vectorConstructor, velocity.getX(), velocity.getY(), velocity.getZ());
        return newInstance(velocityPacketConstructor, invokeMethod(getId, entity), vector);
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

        Object dataWatcher = invokeMethod(getDataWatcher, entity);
        @SuppressWarnings("unchecked") List<Object> items = (List<Object>) invokeMethod(getItems, dataWatcher);

        // If the entity has no metadata, make sure to return some packet
        if (items == null || items.isEmpty()) {
            debug.debug("Entity " + entity + " does not have metadata");
            return getMetadataPacket(entity);
        }

        invokeMethod(methodE, dataWatcher);
        Object dataItem = items.get(0);

        // Get the byte data, then apply the bitmask
        byte data = (byte) invokeMethod(getMetaValue, dataItem);
        data = (byte) (isEnableFlags ? data | mask : data & ~mask);
        invokeMethod(setMetaValue, dataItem, data);

        Object packet = newInstance(metadataPacketConstructor1);
        setField(metadataPacketID, packet, invokeMethod(getId, entity));
        setField(metadataPacketWatcher, packet, items);
        return packet;
    }

    @Override
    public Object setMetadata(Object packet, boolean isEnableFlags, EntityMeta... flags) {

        // Setup the byte data
        byte mask = 0;
        for (EntityMeta flag : flags) {
            mask |= flag.getMask();
        }

        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) invokeField(metadataPacketWatcher, packet);
        Object dataItem = items.get(0);

        // Get the byte data, then apply the bitmask
        byte data = (byte) invokeMethod(getMetaValue, dataItem);
        data = (byte) (isEnableFlags ? data | mask : data & ~mask);
        invokeMethod(setMetaValue, dataItem, data);

        ReflectionUtil.setField(metadataPacketWatcher, packet, items);
        return packet;
    }

    @Override
    public Object getDestroyPacket(Object entity) {
        return newInstance(destroyPacketConstructor, invokeMethod(getId, entity));
    }

    @Override
    public void spawnFirework(Location loc, Collection<? extends Player> players, byte flightTime, FireworkEffect... effects) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("Location#getWorld must not return null!");
        }

        Object nmsWorld = invokeMethod(worldGetHandle, loc.getWorld());
        Object fireworks = newInstance(entityFireworksConstructor, nmsWorld, loc.getX(), loc.getY(), loc.getZ(), invokeField(defaultItemField, null));
        setField(expectedLifeSpanField, fireworks, flightTime);

        Object nmsItem = newInstance(nmsItemConstructor, invokeMethod(craftMagicNumbersGetItem, null, Material.FIREWORK_ROCKET));
        FireworkMeta fireworkMeta = (FireworkMeta) invokeMethod(getItemMeta, invokeMethod(getCraftItemFactoryInstance, null), Material.FIREWORK_ROCKET);
        fireworkMeta.addEffects(effects);
        invokeMethod(setItemMeta, null, nmsItem, fireworkMeta);

        Object dataWatcher = invokeMethod(getDataWatcher, fireworks);
        invokeMethod(setDataWatcherObject, dataWatcher, fireworkItemDataWatcher, nmsItem);

        Object spawn = getSpawnPacket(fireworks);
        Object meta = getMetadataPacket(fireworks);

        for (Player player : players) {
            CompatibilityAPI.getCompatibility().sendPackets(player, spawn, meta);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Object status = newInstance(entityStatusPacketConstructor, fireworks, (byte) 17);
                Object destroy = getDestroyPacket(fireworks);
                for (Player player : players) {
                    CompatibilityAPI.getCompatibility().sendPackets(player, status, destroy);
                }
            }
        }.runTaskLaterAsynchronously(MechanicsCore.getPlugin(), flightTime);
    }

    @Override
    public FallingBlockWrapper createFallingBlock(@Nonnull Location loc, @Nonnull Material mat, byte data, @Nullable Vector motion, int maxTicks) {
        Object blockData = invokeMethod(getNMSBlockData, mat.createBlockData());
        Object world = invokeMethod(worldGetHandle, loc.getWorld());
        Object entity = newInstance(entityFallingBlockConstructor, world, loc.getX(), loc.getY(), loc.getZ(), blockData);

        return new FallingBlockWrapper(entity, maxTicks);
    }

    @Override
    public FallingBlockWrapper createFallingBlock(@Nonnull Location loc, @Nonnull BlockState state, @Nullable Vector motion, int maxTicks) {
        Object blockData = invokeMethod(getNMSBlockState, state);
        Object world = invokeMethod(worldGetHandle, loc.getWorld());
        Object entity =  newInstance(entityFallingBlockConstructor, world, loc.getX(), loc.getY(), loc.getZ(), blockData);

        return new FallingBlockWrapper(entity, maxTicks);
    }

    @Override
    public Object toNMSItemEntity(ItemStack item, World world, double x, double y, double z) {
        Object nmsWorld = invokeMethod(getHandle, world);
        Object nmsItem = invokeMethod(asNMSCopy, null, item);

        return newInstance(entityItemConstructor, nmsWorld, x, y, z, nmsItem);
    }
}
