package me.deecaad.compatibility.entity;

import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;

import static me.deecaad.core.MechanicsCore.debug;

public class Entity_1_15_R1 implements EntityCompatibility {

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
    public Object getDestroyPacket(Object entity) {
        if (!(entity instanceof Entity)) {
            debug.error("Entity " + entity + " must be a 1.15 NMS entity");
            return null;
        }

        return new PacketPlayOutEntityDestroy(((Entity) entity).getId());
    }
}
