package me.deecaad.compatibility.entity;

import me.deecaad.core.utils.BitOperation;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_15_R1.DataWatcher;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_15_R1.PathfinderGoal;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;

import java.lang.reflect.Field;
import java.util.List;

import static me.deecaad.core.MechanicsCore.debug;

public class Entity_1_15_R1 implements EntityCompatibility {

    private static final Class<?> metaPacketClass;
    private static final Field metaPacketA;
    private static final Field metaPacketB;

    static {
        metaPacketClass = ReflectionUtil.getNMSClass("PacketPlayOutEntityMetadata");
        metaPacketA = ReflectionUtil.getField(metaPacketClass, "a");
        metaPacketB = ReflectionUtil.getField(metaPacketClass, "b");
    }

    @Override
    public Object getNMSEntity(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    public int getId(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle().getId();
    }

    @Override
    public Object getSpawnPacket(Object entity) {
        if (!(entity instanceof Entity)) {
            debug.error("Entity " + entity + " must be a 1.15 NMS entity");
            return null;
        }

        return new PacketPlayOutSpawnEntity((Entity) entity);
    }

    @Override
    public Object getMetadataPacket(Object entity) {
        if (!(entity instanceof Entity)) {
            debug.error("Entity " + entity + " must be a 1.15 NMS entity");
            return null;
        }

        Entity nmsEntity = (Entity) entity;
        return new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true);
    }

    @Override
    public Object getMetadataPacket(Object entity, BitOperation operation, boolean isAddFlags, EntityMeta... flags) {

        // Make sure the given object is an entity
        if (!(entity instanceof Entity)) {
            debug.error("Entity " + entity + " must be a 1.15 NMS entity");
            return null;
        }

        // Setup the byte data
        byte mask = 0;
        for (EntityMeta flag : flags) {
            mask = flag.setFlag(mask, isAddFlags);
        }

        // Get the metadata stored in the entity
        Entity nmsEntity = (Entity) entity;
        DataWatcher dataWatcher = nmsEntity.getDataWatcher();
        List<DataWatcher.Item<?>> items = dataWatcher.c();

        // I don't think this should happen, at least not often. Make
        // sure to return some packet though
        if (items == null || items.isEmpty()) {
            debug.debug("Entity " + entity + " does not have metadata");
            return new PacketPlayOutEntityMetadata(nmsEntity.getId(), dataWatcher, true);
        }

        // Get the current byte data
        dataWatcher.e();
        @SuppressWarnings("unchecked")
        DataWatcher.Item<Byte> item = (DataWatcher.Item<Byte>) items.get(0);

        // Create and set byte data
        byte data = item.b();
        data = operation.invoke(data, mask);
        item.a(data);

        // Create the packet
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();
        ReflectionUtil.setField(metaPacketA, metaPacket, nmsEntity.getId());
        ReflectionUtil.setField(metaPacketB, metaPacket, items);

        return new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true);
    }

    @Override
    public Object setMetadata(Object packet, BitOperation operation, EntityMeta... flags) {

        // Setup the byte data
        byte mask = 0;
        for (EntityMeta flag : flags) {
            mask = flag.setFlag(mask, true);
        }

        @SuppressWarnings("unchecked")
        List<DataWatcher.Item<?>> items = (List<DataWatcher.Item<?>>) ReflectionUtil.invokeField(metaPacketB, packet);

        @SuppressWarnings("unchecked")
        DataWatcher.Item<Byte> item = (DataWatcher.Item<Byte>) items.get(0);

        // Create and set byte data
        byte data = item.b();
        data = operation.invoke(data, mask);
        item.a(data);

        ReflectionUtil.setField(metaPacketB, packet, items);
        return packet;
    }

    @Override
    public Object getDestroyPacket(Object entity) {
        if (!(entity instanceof Entity)) {
            debug.error("Entity " + entity + " must be a 1.15 NMS entity");
            return null;
        }

        return new PacketPlayOutEntityDestroy(((Entity) entity).getId());
    }

    @Override
    public Object getGoalSelector(CustomPathfinderGoal goal) {
        return null;
    }


    private static class v1_15_R1_Path extends PathfinderGoal {

        private CustomPathfinderGoal goal;

        private v1_15_R1_Path(CustomPathfinderGoal goal) {
            this.goal = goal;
        }

        @Override
        public boolean a() {
            return goal.shouldStart();
        }
    }
}
