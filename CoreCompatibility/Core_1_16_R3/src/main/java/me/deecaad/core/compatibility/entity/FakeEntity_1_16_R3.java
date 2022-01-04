package me.deecaad.core.compatibility.entity;

import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static net.minecraft.server.v1_16_R3.PacketPlayOutEntity.*;

public class FakeEntity_1_16_R3 extends FakeEntity {

    private static final Field metaPacketA;
    private static final Field metaPacketB;

    static {
        metaPacketA = ReflectionUtil.getField(PacketPlayOutEntityMetadata.class, int.class);
        metaPacketB = ReflectionUtil.getField(PacketPlayOutEntityMetadata.class, List.class);

        if (ReflectionUtil.getMCVersion() != 16) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + FakeEntity_1_16_R3.class + " when not using Minecraft 16",
                    new InternalError()
            );
        }
    }


    private final Entity entity;
    private final List<PlayerConnection> connections; // store the player connection to avoid type cast

    // Only 1 of these can be used at a time
    private IBlockData block;
    private net.minecraft.server.v1_16_R3.ItemStack item;

    public FakeEntity_1_16_R3(@NotNull Location location, @NotNull EntityType type, @Nullable Object data) {
        super(location, type);

        if (location.getWorld() == null)
            throw new IllegalArgumentException();

        CraftWorld world = (CraftWorld) location.getWorld();
        Entity entity;

        // Location vars
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        // Some entity types (dropped items and falling blocks, for example)
        // require extra data in order to display. We only need to use these
        // constructors when we are given the data (data != null).
        if (data != null) {

            // Cannot use java 16 switch statements, unfortunately.
            switch (type) {
                case DROPPED_ITEM:
                    entity = new EntityItem(world.getHandle(), x, y, z, item = CraftItemStack.asNMSCopy((ItemStack) data));
                    break;
                case FALLING_BLOCK:
                    entity = new EntityFallingBlock(world.getHandle(), x, y, z, block =
                            (data.getClass() == Material.class
                                    ? ((CraftBlockData) ((Material) data).createBlockData()).getState()
                                    : ((CraftBlockState) data).getHandle()
                            ));
                    break;
                default:
                    entity = world.createEntity(location, type.getEntityClass());
                    break;
            }
        } else {
            entity = world.createEntity(location, type.getEntityClass());
        }

        this.entity = entity;
        this.cache = entity.getId();
        this.connections = new LinkedList<>(); // We only need to iterate/remove, so LinkedList is best
    }

    @Override
    protected void setLocation(double x, double y, double z, float yaw, float pitch) {
        super.setLocation(x, y, z, yaw, pitch);

        // Needed for teleport packet.
        entity.setPositionRaw(x, y, z);
        entity.yaw = yaw;
        entity.pitch = pitch;
    }

    @Override
    public void setMotion(double dx, double dy, double dz) {
        PacketPlayOutEntityVelocity packet = new PacketPlayOutEntityVelocity(cache, new Vec3D(dx, dy, dz));

        for (PlayerConnection connection : connections) {
            connection.sendPacket(packet);
        }
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        location.setYaw(yaw);
        location.setPitch(pitch);

        byte byteYaw = convertYaw(yaw);
        PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(cache, byteYaw, convertPitch(pitch), false);
        PacketPlayOutEntityHeadRotation head = type.isAlive() ? new PacketPlayOutEntityHeadRotation(entity, byteYaw) : null;

        for (PlayerConnection connection : connections) {
            connection.sendPacket(packet);
            if (head != null)
                connection.sendPacket(head);
        }
    }

    @Override
    public void setPositionRaw(double x, double y, double z, float yaw, float pitch) {
        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(entity);
        PacketPlayOutEntityHeadRotation head = type.isAlive() ? new PacketPlayOutEntityHeadRotation(entity, convertYaw(yaw)) : null;

        for (PlayerConnection connection : connections) {
            connection.sendPacket(packet);
            if (head != null)
                connection.sendPacket(head);
        }
    }

    @Override
    public void setPositionRotation(short dx, short dy, short dz, byte yaw, byte pitch) {
        PacketPlayOutRelEntityMoveLook packet = new PacketPlayOutRelEntityMoveLook(cache, dx, dy, dz, yaw, pitch, false);
        PacketPlayOutEntityHeadRotation head = type.isAlive() ? new PacketPlayOutEntityHeadRotation(entity, convertYaw(yaw)) : null;

        for (PlayerConnection connection : connections) {
            connection.sendPacket(packet);
            if (head != null)
                connection.sendPacket(head);
        }
    }

    public void show() {
        Packet<?> spawn = type.isAlive()
                ? new PacketPlayOutSpawnEntityLiving((EntityLiving) entity)
                : new PacketPlayOutSpawnEntity(entity, type == EntityType.FALLING_BLOCK ? Block.getCombinedId(block) : 1);
        PacketPlayOutEntityMetadata meta = getMetaPacket();

        for (org.bukkit.entity.Entity temp : DistanceUtil.getEntitiesInRange(location)) {
            if (temp.getType() != EntityType.PLAYER) {
                continue;
            }

            PlayerConnection connection = ((CraftPlayer) temp).getHandle().playerConnection;
            if (connections.contains(connection)) {
                continue;
            }

            connection.sendPacket(spawn);
            connection.sendPacket(meta);
            connections.add(connection);
        }
    }

    @Override
    public void show(@NotNull Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        connection.sendPacket(type.isAlive()
                ? new PacketPlayOutSpawnEntityLiving((EntityLiving) entity)
                : new PacketPlayOutSpawnEntity(entity, type == EntityType.FALLING_BLOCK ? Block.getCombinedId(block) : 1));
        connection.sendPacket(getMetaPacket());

        // Inject the player's packet connection into this listener, so we can
        // show the player position/velocity/rotation changes
        connections.add(connection);
    }

    @Override
    public void updateMeta() {
        PacketPlayOutEntityMetadata packet = getMetaPacket();
        for (PlayerConnection connection : connections) {
            connection.sendPacket(packet);
        }
    }

    @SuppressWarnings("unchecked")
    private PacketPlayOutEntityMetadata getMetaPacket() {

        // Get the metadata stored in the entity
        DataWatcher dataWatcher = entity.getDataWatcher();
        List<DataWatcher.Item<?>> items = dataWatcher.c(); // get all

        // I don't think this should happen, at least not often. Make
        // sure to return some packet though
        if (items == null || items.isEmpty()) {
            return new PacketPlayOutEntityMetadata(entity.getId(), dataWatcher, true);
        }

        dataWatcher.e(); // clear dirty

        // Get the current byte data
        DataWatcher.Item<Byte> bitMaskItem      = (DataWatcher.Item<Byte>) items.get(0);
        DataWatcher.Item<String> customNameItem = (DataWatcher.Item<String>) items.get(2);
        DataWatcher.Item<Boolean> showNameItem  = (DataWatcher.Item<Boolean>) items.get(3);
        DataWatcher.Item<Boolean> noGravityItem = (DataWatcher.Item<Boolean>) items.get(4);

        // Get the byte data, then apply the bitmask
        bitMaskItem.a(getMeta().apply(bitMaskItem.b()));
        customNameItem.a(display);
        showNameItem.a((Boolean) (display != null));
        noGravityItem.a((Boolean) (!gravity));

        // Create the packet. We need to set the raw parameters, so we use reflection.
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();
        ReflectionUtil.setField(metaPacketA, metaPacket, cache);
        ReflectionUtil.setField(metaPacketB, metaPacket, items);

        return metaPacket;
    }

    @Override
    public void remove() {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(cache);

        Iterator<PlayerConnection> iterator = connections.iterator();
        while (iterator.hasNext()) {
            PlayerConnection connection = iterator.next();
            connection.sendPacket(packet);

            iterator.remove();
        }
    }

    @Override
    public void remove(@NotNull Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(cache));

        // Uninject player from seeing position changes
        if (!connections.remove(connection)) {
            throw new IllegalStateException("Tried to remove player that was never added");
        }
    }
}
