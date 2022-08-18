package me.deecaad.core.compatibility.entity;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.equipevent.NonNullList_1_18_R2;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.List;

// https://nms.screamingsandals.org/1.18.1/
public class Entity_1_18_R2 implements EntityCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 18) {
            MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Entity_1_18_R2.class + " when not using Minecraft 18",
                    new InternalError()
            );
        }
    }

    @Override
    public List generateNonNullList(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        return new NonNullList_1_18_R2(size, consumer);
    }

    @Override
    public FakeEntity generateFakeEntity(Location location, EntityType type, Object data) {
        return new FakeEntity_1_18_R2(location, type, data);
    }

    @Override
    public int getId(Object obj) {
        return ((ClientboundSetEntityDataPacket) obj).getId();
    }

    @Override
    public Object generateMetaPacket(Entity bukkit) {
        net.minecraft.world.entity.Entity entity = ((CraftEntity) bukkit).getHandle();
        return new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), true);
    }

    @Override
    public void modifyMetaPacket(Object obj, EntityMeta meta, boolean enabled) {
        ClientboundSetEntityDataPacket packet = (ClientboundSetEntityDataPacket) obj;
        List<SynchedEntityData.DataItem<?>> list = packet.getUnpackedData();

        if (list == null || list.isEmpty())
            return;

        // The "shared byte data" is applied to every entity, and it is always
        // the first item (It can never be the second, third, etc.). However,
        // if no modifications are made to the "shared byte data" before this
        // packet is sent, that item will not be present. This is implemented
        // in vanilla's dirty meta system.
        if (list.get(0) == null || list.get(0).getValue().getClass() != Byte.class)
            return;

        // noinspection unchecked
        SynchedEntityData.DataItem<Byte> item = (SynchedEntityData.DataItem<Byte>) list.get(0);
        byte data = item.getValue();
        data = meta.set(data, enabled);
        item.setValue(data);
    }
}