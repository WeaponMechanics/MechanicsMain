package me.deecaad.core.compatibility.entity;

import com.cjcrafter.foliascheduler.util.FieldAccessor;
import com.cjcrafter.foliascheduler.util.ReflectionUtil;
import com.mojang.datafixers.util.Pair;
import me.deecaad.core.compatibility.equipevent.NonNullList_1_21_R2;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Entity_1_21_R2 implements EntityCompatibility {

    public static final FieldAccessor itemsById = ReflectionUtil.getField(SynchedEntityData.class, SynchedEntityData.DataItem[].class);

    @Override
    public List generateNonNullList(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        return new NonNullList_1_21_R2(size, consumer);
    }

    @Override
    public FakeEntity generateFakeEntity(Location location, EntityType type, Object data) {
        return new FakeEntity_1_21_R2(location, type, data);
    }

    @Override
    public void setSlot(Player bukkit, EquipmentSlot slot, @Nullable ItemStack item) {
        if (item == null) {
            item = bukkit.getEquipment().getItem(slot);
        }

        int id = bukkit.getEntityId();
        net.minecraft.world.entity.EquipmentSlot nmsSlot = switch (slot) {
            case HEAD -> net.minecraft.world.entity.EquipmentSlot.HEAD;
            case CHEST -> net.minecraft.world.entity.EquipmentSlot.CHEST;
            case LEGS -> net.minecraft.world.entity.EquipmentSlot.LEGS;
            case FEET -> net.minecraft.world.entity.EquipmentSlot.FEET;
            case HAND -> net.minecraft.world.entity.EquipmentSlot.MAINHAND;
            case OFF_HAND -> net.minecraft.world.entity.EquipmentSlot.OFFHAND;
            case BODY -> net.minecraft.world.entity.EquipmentSlot.BODY;
        };

        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> temp = new ArrayList<>(1);
        temp.add(new Pair<>(nmsSlot, CraftItemStack.asNMSCopy(item)));
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(id, temp);
        ((CraftPlayer) bukkit).getHandle().connection.send(packet);
    }

    public static List<SynchedEntityData.DataValue<?>> getEntityData(SynchedEntityData data, boolean forceUpdateAll) {
        if (!forceUpdateAll) {
            List<SynchedEntityData.DataValue<?>> dirty = data.packDirty();
            return dirty == null ? List.of() : dirty;
        }

        // 1.19.3 changed the packet arguments, so in order to unpack ALL data
        // (not just the dirty data) we need to manually get it and unpack it.
        SynchedEntityData.DataItem<?>[] metaData = (SynchedEntityData.DataItem<?>[]) itemsById.get(data);
        List<SynchedEntityData.DataValue<?>> packed = new ArrayList<>(metaData.length);
        for (SynchedEntityData.DataItem<?> element : metaData)
            packed.add(element.value());
        return packed;
    }

    @Override
    public Object generateMetaPacket(Entity bukkit) {
        net.minecraft.world.entity.Entity entity = ((CraftEntity) bukkit).getHandle();
        return new ClientboundSetEntityDataPacket(entity.getId(), getEntityData(entity.getEntityData(), true));
    }

    @Override
    public void modifyMetaPacket(Object obj, EntityMeta meta, boolean enabled) {
        ClientboundSetEntityDataPacket packet = (ClientboundSetEntityDataPacket) obj;
        List<SynchedEntityData.DataValue<?>> list = packet.packedItems();

        if (list.isEmpty())
            return;

        // The "shared byte data" is applied to every entity, and it is always
        // the first item (It can never be the second, third, etc.). However,
        // if no modifications are made to the "shared byte data" before this
        // packet is sent, that item will not be present. This is implemented
        // in vanilla's dirty meta system.
        if (list.get(0) == null || list.get(0).value().getClass() != Byte.class)
            return;

        // noinspection unchecked
        SynchedEntityData.DataValue<Byte> item = (SynchedEntityData.DataValue<Byte>) list.get(0);
        byte data = item.value();
        data = meta.set(data, enabled);

        // 1.19.3 changed this to a record
        list.set(0, new SynchedEntityData.DataValue<>(item.id(), item.serializer(), data));
    }
}