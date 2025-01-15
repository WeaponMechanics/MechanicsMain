package me.deecaad.core.compatibility.entity;

import com.mojang.datafixers.util.Pair;
import me.deecaad.core.utils.DistanceUtil;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static me.deecaad.core.compatibility.entity.Entity_1_21_R3.getEntityData;
import static net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.PosRot;
import static net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.Rot;

public class FakeEntity_1_21_R3 extends FakeEntity {

    // Store this since using Enum#values() is especially slow
    public static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

    private final Entity entity;
    private final ServerEntity serverEntity;
    private final List<ServerGamePacketListenerImpl> connections; // store the player connection to avoid type cast

    // Only 1 of these can be used at a time
    private BlockState block;
    private ItemStack item;

    public FakeEntity_1_21_R3(@NotNull Location location, @NotNull EntityType type, @Nullable Object data) {
        super(location, type);

        if (location.getWorld() == null)
            throw new IllegalArgumentException();

        CraftWorld world = (CraftWorld) location.getWorld();
        ServerLevel handle = world.getHandle();

        // Location vars
        final double x = location.getX();
        final double y = location.getY();
        final double z = location.getZ();

        // Some entity types (dropped items and falling blocks, for example)
        // require extra data in order to display. We only need to use these
        // constructors when we are given the data (data != null).
        if (data != null) {
            entity = switch (type) {
                case ITEM -> new ItemEntity(handle, x, y, z, item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data));
                case FALLING_BLOCK -> {
                    FallingBlockEntity temp = new FallingBlockEntity(net.minecraft.world.entity.EntityType.FALLING_BLOCK, handle);
                    temp.setPosRaw(x, y, z);
                    block = (data.getClass() == Material.class
                        ? ((CraftBlockData) ((Material) data).createBlockData()).getState()
                        : ((CraftBlockState) data).getHandle());
                    yield temp;
                }
                case FIREWORK_ROCKET -> new FireworkRocketEntity(handle, item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data), x, y, z, true);
                case ITEM_DISPLAY -> {
                    Display.ItemDisplay temp = net.minecraft.world.entity.EntityType.ITEM_DISPLAY.create(handle, EntitySpawnReason.COMMAND);
                    temp.setPos(x, y, z);
                    temp.setItemStack(CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data));
                    yield temp;
                }
                default -> world.makeEntity(location, type.getEntityClass());
            };
        } else {
            entity = world.makeEntity(location, type.getEntityClass());
        }

        if (type == EntityType.ARMOR_STAND) {
            ArmorStand armorStand = (ArmorStand) entity;
            armorStand.setMarker(true);
        }

        this.setLocation(x, y, z, location.getYaw(), location.getPitch());
        this.cache = entity.getId();
        this.serverEntity = new ServerEntity(handle, entity, entity.getType().updateInterval(), entity.getType().trackDeltas(), (packet) -> {
            /* intentionally empty... do nothing */
        }, Collections.emptySet());
        this.connections = new LinkedList<>(); // We only need to iterate/remove, so LinkedList is best
    }

    @Override
    public boolean getMeta(int metaFlag) {
        return entity.getSharedFlag(metaFlag);
    }

    @Override
    public void setMeta(int metaFlag, boolean isEnabled) {
        entity.setSharedFlag(metaFlag, isEnabled);
    }

    @Override
    public Object getData() {
        return switch (type) {
            case ITEM -> item.asBukkitCopy();
            case FALLING_BLOCK -> CraftBlockData.fromData(block);
            default -> null;
        };
    }

    @Override
    public void setData(@Nullable Object data) {
        switch (type) {
            case ITEM -> ((ItemEntity) entity).setItem(item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data));
        }
    }

    @Override
    public void setDisplay(@Nullable String display) {
        entity.setCustomName(CraftChatMessage.fromStringOrNull(display));
        entity.setCustomNameVisible(display != null && !display.isEmpty());
    }

    @Override
    public void setGravity(boolean gravity) {
        entity.setNoGravity(!gravity);
    }

    @Override
    protected void setLocation(double x, double y, double z, float yaw, float pitch) {
        super.setLocation(x, y, z, yaw, pitch);

        // Needed for teleport packet.
        entity.setPosRaw(x, y, z);
        entity.setYHeadRot(yaw);
        entity.setYRot(yaw);
        entity.setXRot(pitch);
    }

    @Override
    public void setMotion(double dx, double dy, double dz) {
        ClientboundSetEntityMotionPacket packet = new ClientboundSetEntityMotionPacket(cache, new Vec3(dx, dy, dz));
        motion.setX(dx);
        motion.setY(dy);
        motion.setZ(dz);

        sendPackets(packet);
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        if (offset != null) {
            yaw += offset.getYaw();
            pitch += offset.getPitch();
        }

        location.setYaw(yaw);
        location.setPitch(pitch);
        entity.setYHeadRot(yaw);
        entity.setXRot(yaw);
        entity.setYRot(pitch);

        byte byteYaw = convertYaw(yaw);
        Rot packet = new Rot(cache, byteYaw, convertPitch(pitch), false);
        ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(entity, byteYaw);

        sendPackets(packet, head);

        if (type == EntityType.ARMOR_STAND || entity instanceof Display)
            updateMeta();
    }

    @Override
    public void setPositionRaw(double x, double y, double z, float yaw, float pitch) {
        ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(entity.getId(), PositionMoveRotation.of(entity), Set.of(), entity.onGround());
        ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(entity, convertYaw(yaw));

        sendPackets(packet, head);
    }

    @Override
    public void setPositionRotation(short dx, short dy, short dz, byte yaw, byte pitch) {
        PosRot packet = new PosRot(cache, dx, dy, dz, yaw, pitch, false);
        ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(entity, convertYaw(yaw));

        sendPackets(packet, head);
    }

    @Override
    public void show() {
        Packet<?> spawn = type.isAlive()
            ? new ClientboundAddEntityPacket(entity, serverEntity)
            : new ClientboundAddEntityPacket(entity, serverEntity, type == EntityType.FALLING_BLOCK ? Block.getId(block) : 0);

        ClientboundSetEntityDataPacket meta = new ClientboundSetEntityDataPacket(cache, getEntityData(entity.getEntityData(), false));
        ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(entity, convertYaw(getYaw()));
        Rot look = new Rot(cache, convertYaw(getYaw()), convertPitch(getPitch()), false);
        ClientboundSetEntityMotionPacket velocity = new ClientboundSetEntityMotionPacket(cache, new Vec3(motion.getX(), motion.getY(), motion.getZ()));
        ClientboundSetEquipmentPacket equipment = getEquipmentPacket();

        for (Player temp : DistanceUtil.getPlayersInRange(location)) {
            ServerGamePacketListenerImpl connection = ((CraftPlayer) temp).getHandle().connection;
            if (connections.contains(connection)) {
                continue;
            }

            connection.send(spawn);
            connection.send(meta);
            connection.send(head);
            connection.send(velocity);
            connection.send(look);
            if (equipment != null)
                connection.send(equipment);

            connections.add(connection);
        }
    }

    @Override
    public void show(@NotNull Player player) {
        if (!player.isOnline())
            return;

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        connection.send(type.isAlive()
            ? new ClientboundAddEntityPacket(entity, serverEntity)
            : new ClientboundAddEntityPacket(entity, serverEntity, type == EntityType.FALLING_BLOCK ? Block.getId(block) : 0));
        connection.send(new ClientboundSetEntityDataPacket(cache, getEntityData(entity.getEntityData(), true)));
        connection.send(new Rot(cache, convertYaw(getYaw()), convertPitch(getPitch()), false));
        connection.send(new ClientboundSetEntityMotionPacket(cache, new Vec3(motion.getX(), motion.getY(), motion.getZ())));
        connection.send(new ClientboundRotateHeadPacket(entity, convertYaw(getYaw())));
        ClientboundSetEquipmentPacket equipment = getEquipmentPacket();
        if (equipment != null)
            connection.send(equipment);

        // Inject the player's packet connection into this listener, so we can
        // show the player position/velocity/rotation changes
        connections.add(connection);
    }

    @Override
    public void updateMeta() {
        if (type == EntityType.ARMOR_STAND)
            ((ArmorStand) entity).setHeadPose(new Rotations(getPitch(), 0, 0));

        sendPackets(new ClientboundSetEntityDataPacket(cache, getEntityData(entity.getEntityData(), false)));
    }

    @Override
    public void remove() {
        sendPackets(new ClientboundRemoveEntitiesPacket(cache));
        connections.clear();
    }

    @Override
    public void remove(@NotNull Player player) {
        if (!player.isOnline())
            return;
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(cache));

        // Uninject player from seeing position changes
        if (!connections.remove(connection)) {
            throw new IllegalStateException("Tried to remove player that was never added");
        }
    }

    @Override
    public void playEffect(@NotNull EntityEffect effect) {
        if (!effect.getApplicable().isAssignableFrom(type.getEntityClass()))
            return;
        sendPackets(new ClientboundEntityEventPacket(entity, effect.getData()));
    }

    @Override
    public void setEquipment(@NotNull org.bukkit.inventory.EquipmentSlot equipmentSlot, org.bukkit.inventory.ItemStack itemStack) {
        if (!type.isAlive())
            throw new IllegalStateException("Cannot set equipment of " + type);

        EquipmentSlot slot = switch (equipmentSlot) {
            case HAND -> EquipmentSlot.MAINHAND;
            case OFF_HAND -> EquipmentSlot.OFFHAND;
            case FEET -> EquipmentSlot.FEET;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case HEAD -> EquipmentSlot.HEAD;
            case BODY -> EquipmentSlot.BODY;
        };

        LivingEntity livingEntity = (LivingEntity) entity;
        livingEntity.setItemSlot(slot, CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public void updateEquipment() {
        ClientboundSetEquipmentPacket packet = getEquipmentPacket();
        if (packet != null)
            sendPackets(packet);
    }

    private ClientboundSetEquipmentPacket getEquipmentPacket() {
        if (!type.isAlive())
            return null;
        LivingEntity livingEntity = (LivingEntity) entity;

        List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>(6);
        for (EquipmentSlot slot : SLOTS) {
            ItemStack itemStack = livingEntity.getItemBySlot(slot);
            if (!itemStack.isEmpty()) {
                equipmentList.add(Pair.of(slot, itemStack));
            }
        }
        return equipmentList.isEmpty() ? null : new ClientboundSetEquipmentPacket(cache, equipmentList);
    }

    private void sendPackets(Packet<?>... packets) {
        Iterator<ServerGamePacketListenerImpl> connectionIterator = connections.iterator();
        while (connectionIterator.hasNext()) {
            ServerGamePacketListenerImpl connection = connectionIterator.next();
            if (connection.isDisconnected()) {
                connectionIterator.remove();
                continue;
            }
            for (Packet<?> packet : packets) {
                connection.send(packet);
            }
        }
    }
}
