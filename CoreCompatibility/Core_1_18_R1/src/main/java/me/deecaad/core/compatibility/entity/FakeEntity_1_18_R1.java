package me.deecaad.core.compatibility.entity;

import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.*;

public class FakeEntity_1_18_R1 extends FakeEntity {

    private static final SynchedEntityData EMPTY;
    private static final Field metaPacketB;

    static {
        metaPacketB = ReflectionUtil.getField(ClientboundSetEntityDataPacket.class, List.class);

        // Using an "EMPTY" entity data when sending Metadata packets will save
        // performance, since the "getAll" method usually has to check/collect
        // everything.
        EMPTY = new SynchedEntityData(null) {
            @Nullable
            @Override
            public List<DataItem<?>> getAll() {
                return null;
            }
        };

        if (ReflectionUtil.getMCVersion() != 18) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + FakeEntity_1_18_R1.class + " when not using Minecraft 18",
                    new InternalError()
            );
        }
    }


    private final Entity entity;
    private final List<ServerGamePacketListenerImpl> connections; // store the player connection to avoid type cast

    // Only 1 of these can be used at a time
    private BlockState block;
    private net.minecraft.world.item.ItemStack item;

    public FakeEntity_1_18_R1(@NotNull Location location, @NotNull EntityType type, @Nullable Object data) {
        super(location, type);

        if (location.getWorld() == null)
            throw new IllegalArgumentException();

        CraftWorld world = (CraftWorld) location.getWorld();

        // Location vars
        final double x = location.getX();
        final double y = location.getY();
        final double z = location.getZ();

        // Some entity types (dropped items and falling blocks, for example)
        // require extra data in order to display. We only need to use these
        // constructors when we are given the data (data != null).
        if (data != null) {
            entity = switch (type) {
                case DROPPED_ITEM -> new ItemEntity(world.getHandle(), x, y, z, item = CraftItemStack.asNMSCopy((ItemStack) data));
                case FALLING_BLOCK -> new FallingBlockEntity(world.getHandle(), x, y, z, block = ((CraftBlockState) data).getHandle());
                default -> world.createEntity(location, type.getEntityClass());
            };
        } else {
            entity = world.createEntity(location, type.getEntityClass());
        }

        this.cache = entity.getId();
        this.connections = new LinkedList<>(); // We only need to iterate/remove, so LinkedList is best
    }

    @Override
    protected void setLocation(double x, double y, double z, float yaw, float pitch) {
        super.setLocation(x, y, z, yaw, pitch);

        // Needed for teleport packet.
        entity.setPosRaw(x, y, z);
        entity.setYRot(yaw);
        entity.setXRot(pitch);
    }

    @Override
    public void setMotion(double dx, double dy, double dz) {
        ClientboundSetEntityMotionPacket packet = new ClientboundSetEntityMotionPacket(cache, new Vec3(dx, dy, dz));

        for (ServerGamePacketListenerImpl connection : connections) {
            connection.send(packet);
        }
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        location.setYaw(yaw);
        location.setPitch(pitch);

        byte byteYaw = convertYaw(yaw);
        Rot packet = new Rot(cache, byteYaw, convertPitch(pitch), false);
        ClientboundRotateHeadPacket head = type.isAlive() ? new ClientboundRotateHeadPacket(entity, byteYaw) : null;

        for (ServerGamePacketListenerImpl connection : connections) {
            connection.send(packet);
            if (head != null)
                connection.send(head);
        }
    }

    @Override
    public void setPositionRaw(double x, double y, double z, float yaw, float pitch) {
        ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(entity);
        ClientboundRotateHeadPacket head = type.isAlive() ? new ClientboundRotateHeadPacket(entity, convertYaw(yaw)) : null;

        for (ServerGamePacketListenerImpl connection : connections) {
            connection.send(packet);
            if (head != null)
                connection.send(head);
        }
    }

    @Override
    public void setPositionRotation(short dx, short dy, short dz, byte yaw, byte pitch) {
        PosRot packet = new PosRot(cache, dx, dy, dz, yaw, pitch, false);
        ClientboundRotateHeadPacket head = type.isAlive() ? new ClientboundRotateHeadPacket(entity, convertYaw(yaw)) : null;

        for (ServerGamePacketListenerImpl connection : connections) {
            connection.send(packet);
            if (head != null)
                connection.send(head);
        }
    }

    @Override
    public void show() {
        Packet<?> spawn = type.isAlive()
                ? new ClientboundAddMobPacket((LivingEntity) entity)
                : new ClientboundAddEntityPacket(entity, type == EntityType.FALLING_BLOCK ? Block.getId(block) : 1);
        ClientboundSetEntityDataPacket meta = getMetaPacket();

        for (org.bukkit.entity.Entity temp : DistanceUtil.getEntitiesInRange(location)) {
            if (temp.getType() != EntityType.PLAYER) {
                continue;
            }

            ServerGamePacketListenerImpl connection = ((CraftPlayer) temp).getHandle().connection;
            if (connections.contains(connection)) {
                continue;
            }

            connection.send(spawn);
            connection.send(meta);
            connections.add(connection);
        }
    }

    @Override
    public void show(@NotNull Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        connection.send(type.isAlive()
                ? new ClientboundAddMobPacket((LivingEntity) entity)
                : new ClientboundAddEntityPacket(entity, type == EntityType.FALLING_BLOCK ? Block.getId(block) : 1));
        connection.send(getMetaPacket());

        // Inject the player's packet connection into this listener, so we can
        // show the player position/velocity/rotation changes
        connections.add(connection);
    }

    @Override
    public void updateMeta() {
        ClientboundSetEntityDataPacket packet = getMetaPacket();
        for (ServerGamePacketListenerImpl connection : connections) {
            connection.send(packet);
        }
    }

    private ClientboundSetEntityDataPacket getMetaPacket() {

        // Get the metadata stored in the entity
        SynchedEntityData dataWatcher = entity.getEntityData();
        List<SynchedEntityData.DataItem<?>> items = dataWatcher.getAll();

        // I don't think this should happen, at least not often. Make
        // sure to return some packet though
        if (items == null || items.isEmpty()) {
            return new ClientboundSetEntityDataPacket(entity.getId(), dataWatcher, true);
        }

        // Get the current byte data
        dataWatcher.clearDirty();
        @SuppressWarnings("unchecked")
        SynchedEntityData.DataItem<Byte> item = (SynchedEntityData.DataItem<Byte>) items.get(0);

        // Get the byte data, then apply the bitmask
        item.setValue(getMeta().apply(item.getValue()));

        // Create the packet. We need to set the raw parameters, so we use reflection.
        ClientboundSetEntityDataPacket metaPacket = new ClientboundSetEntityDataPacket(cache, EMPTY, true);
        ReflectionUtil.setField(metaPacketB, metaPacket, items);

        return metaPacket;
    }

    @Override
    public void remove() {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(cache);

        Iterator<ServerGamePacketListenerImpl> iterator = connections.iterator();
        while (iterator.hasNext()) {
            ServerGamePacketListenerImpl connection = iterator.next();
            connection.send(packet);

            iterator.remove();
        }
    }

    @Override
    public void remove(@NotNull Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(cache));

        // Uninject player from seeing position changes
        if (!connections.remove(connection)) {
            throw new IllegalStateException("Tried to remove player that was never added");
        }
    }
}
