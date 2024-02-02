package me.deecaad.core.compatibility.entity;

import com.mojang.datafixers.util.Pair;
import me.deecaad.core.compatibility.equipevent.NonNullList_1_16_R3;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Entity_1_16_R3 implements EntityCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 16) {
            me.deecaad.core.MechanicsCore.debug.log(
                LogLevel.ERROR,
                "Loaded " + Entity_1_16_R3.class + " when not using Minecraft 16",
                new InternalError());
        }
    }

    @Override
    public Vector getLastLocation(Entity entity) {
        net.minecraft.server.v1_16_R3.Entity nms = ((CraftEntity) entity).getHandle();
        return new Vector(nms.lastX, nms.lastY, nms.lastZ);
    }

    @Override
    public List generateNonNullList(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        return new NonNullList_1_16_R3(size, consumer);
    }

    @Override
    public FakeEntity generateFakeEntity(Location location, EntityType type, Object data) {
        return new FakeEntity_1_16_R3(location, type, data);
    }

    private static final Field ID = ReflectionUtil.getField(PacketPlayOutEntityMetadata.class, int.class);
    private static final Field DATA = ReflectionUtil.getField(PacketPlayOutEntityMetadata.class, List.class);

    @Override
    public int getId(Object obj) {
        return (int) ReflectionUtil.invokeField(ID, obj);
    }

    @Override
    public void setSlot(Player bukkit, EquipmentSlot slot, @Nullable ItemStack item) {
        if (item == null) {
            item = bukkit.getEquipment().getItem(slot); // added in 1.15
        }

        int id = bukkit.getEntityId();
        EnumItemSlot nmsSlot = switch (slot) {
            case HEAD -> EnumItemSlot.HEAD;
            case CHEST -> EnumItemSlot.CHEST;
            case LEGS -> EnumItemSlot.LEGS;
            case FEET -> EnumItemSlot.FEET;
            case HAND -> EnumItemSlot.MAINHAND;
            case OFF_HAND -> EnumItemSlot.OFFHAND;
        };

        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> temp = new ArrayList<>(1);
        temp.add(new Pair<>(nmsSlot, CraftItemStack.asNMSCopy(item)));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(id, temp);
        ((CraftPlayer) bukkit).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public Object generateMetaPacket(Entity bukkit) {
        net.minecraft.server.v1_16_R3.Entity entity = ((CraftEntity) bukkit).getHandle();
        return new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);
    }

    @Override
    public void modifyMetaPacket(Object obj, EntityMeta meta, boolean enabled) {
        PacketPlayOutEntityMetadata packet = (PacketPlayOutEntityMetadata) obj;
        List<DataWatcher.Item<?>> list = (List<DataWatcher.Item<?>>) ReflectionUtil.invokeField(DATA, packet);

        if (list == null || list.isEmpty())
            return;

        // The "shared byte data" is applied to every entity, and it is always
        // the first item (It can never be the second, third, etc.). However,
        // if no modifications are made to the "shared byte data" before this
        // packet is sent, that item will not be present. This is implemented
        // in vanilla's dirty meta system.
        if (list.get(0) == null || list.get(0).b().getClass() != Byte.class)
            return;

        // noinspection unchecked
        DataWatcher.Item<Byte> item = (DataWatcher.Item<Byte>) list.get(0);
        byte data = item.b();
        data = meta.set(data, enabled);
        item.a(data);
    }
}
