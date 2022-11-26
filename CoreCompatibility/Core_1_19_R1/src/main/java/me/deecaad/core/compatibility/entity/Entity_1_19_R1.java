package me.deecaad.core.compatibility.entity;

import com.mojang.datafixers.util.Pair;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.equipevent.NonNullList_1_19_R1;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// https://nms.screamingsandals.org/1.18.1/
public class Entity_1_19_R1 implements EntityCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 19) {
            MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Entity_1_19_R1.class + " when not using Minecraft 19",
                    new InternalError()
            );
        }
    }

    @Override
    public Vector getLastLocation(Entity entity) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity) entity).getHandle();
        return new Vector(nms.xOld, nms.yOld, nms.zOld);
    }

    @Override
    public List generateNonNullList(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        return new NonNullList_1_19_R1(size, consumer);
    }

    @Override
    public FakeEntity generateFakeEntity(Location location, EntityType type, Object data) {
        return new FakeEntity_1_19_R1(location, type, data);
    }

    @Override
    public int getId(Object obj) {
        return ((ClientboundSetEntityDataPacket) obj).getId();
    }

    @Override
    public void setSlot(Player bukkit, EquipmentSlot slot, @Nullable ItemStack item) {
        if (item == null) {
            item = bukkit.getEquipment().getItem(slot); // added in 1.15
        }

        int id = bukkit.getEntityId();
        net.minecraft.world.entity.EquipmentSlot nmsSlot = switch (slot) {
            case HEAD -> net.minecraft.world.entity.EquipmentSlot.HEAD;
            case CHEST -> net.minecraft.world.entity.EquipmentSlot.CHEST;
            case LEGS -> net.minecraft.world.entity.EquipmentSlot.LEGS;
            case FEET -> net.minecraft.world.entity.EquipmentSlot.FEET;
            case HAND -> net.minecraft.world.entity.EquipmentSlot.MAINHAND;
            case OFF_HAND -> net.minecraft.world.entity.EquipmentSlot.OFFHAND;
        };

        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> temp = new ArrayList<>(1);
        temp.add(new Pair<>(nmsSlot, CraftItemStack.asNMSCopy(item)));
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(id, temp);
        ((CraftPlayer) bukkit).getHandle().connection.send(packet);
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