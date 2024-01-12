package me.deecaad.core.compatibility.entity;

import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_13_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftChatMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static net.minecraft.server.v1_13_R2.PacketPlayOutEntity.PacketPlayOutEntityLook;
import static net.minecraft.server.v1_13_R2.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;

public class FakeEntity_1_13_R2 extends FakeEntity {

    static {
        if (ReflectionUtil.getMCVersion() != 13) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + FakeEntity_1_13_R2.class + " when not using Minecraft 13",
                    new InternalError()
            );
        }
    }

    // Store this since using Enum#values() is especially slow
    public static final EnumItemSlot[] SLOTS = EnumItemSlot.values();

    private final Entity entity;
    private final List<PlayerConnection> connections; // store the player connection to avoid type cast

    // Only 1 of these can be used at a time
    private IBlockData block;
    private ItemStack item;

    public FakeEntity_1_13_R2(@NotNull Location location, @NotNull EntityType type, @Nullable Object data) {
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
                case DROPPED_ITEM -> new EntityItem(world.getHandle(), x, y, z, item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data));
                case FALLING_BLOCK -> new EntityFallingBlock(world.getHandle(), x, y, z, block =
                        (data.getClass() == Material.class
                                ? ((CraftBlockData) ((Material) data).createBlockData()).getState()
                                : ((CraftBlockState) data).getHandle()
                        ));
                case FIREWORK -> new EntityFireworks(world.getHandle(), x, y, z, item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data));
                default -> world.createEntity(location, type.getEntityClass());
            };
        } else {
            entity = world.createEntity(location, type.getEntityClass());
        }

        if (type == EntityType.ARMOR_STAND) {
            EntityArmorStand armorStand = (EntityArmorStand) entity;
            armorStand.setMarker(true);
        }

        this.setLocation(x, y, z, location.getYaw(), location.getPitch());
        this.cache = entity.getId();
        this.connections = new LinkedList<>(); // We only need to iterate/remove, so LinkedList is best
    }

    @Override
    public boolean getMeta(int metaFlag) {
        return entity.getFlag(metaFlag);
    }

    @Override
    public void setMeta(int metaFlag, boolean isEnabled) {
        entity.setFlag(metaFlag, isEnabled);
    }

    @Override
    public Object getData() {
        return switch (type) {
            case DROPPED_ITEM -> CraftItemStack.asBukkitCopy(item);
            case FALLING_BLOCK -> CraftBlockData.fromData(block);
            default -> null;
        };
    }

    @Override
    public void setData(@Nullable Object data) {
        switch (type) {
            case DROPPED_ITEM -> ((EntityItem) entity).setItemStack(item = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) data));
        }
    }

    @Override
    public void setDisplay(@Nullable String display) {
        entity.setCustomName(CraftChatMessage.fromStringOrNull(display));
        entity.setCustomNameVisible(display != null && !"".equals(display));
    }

    @Override
    public void setGravity(boolean gravity) {
        entity.setNoGravity(!gravity);
    }

    @Override
    protected void setLocation(double x, double y, double z, float yaw, float pitch) {
        super.setLocation(x, y, z, yaw, pitch);

        // Needed for teleport packet.
        entity.setPosition(x, y, z);
        entity.setHeadRotation(yaw);
        entity.yaw = yaw;
        entity.pitch = pitch;
    }

    @Override
    public void setMotion(double dx, double dy, double dz) {
        PacketPlayOutEntityVelocity packet = new PacketPlayOutEntityVelocity(cache, dx, dy, dz);
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
        entity.setHeadRotation(yaw);
        entity.yaw = yaw;
        entity.pitch = pitch;

        byte byteYaw = convertYaw(yaw);
        PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(cache, byteYaw, convertPitch(pitch), false);
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(entity, byteYaw);

        sendPackets(packet, head);

        if (type == EntityType.ARMOR_STAND) updateMeta();
    }

    @Override
    public void setPositionRaw(double x, double y, double z, float yaw, float pitch) {
        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(entity);
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(entity, convertYaw(yaw));

        sendPackets(packet, head);
    }

    @Override
    public void setPositionRotation(short dx, short dy, short dz, byte yaw, byte pitch) {
        PacketPlayOutRelEntityMoveLook packet = new PacketPlayOutRelEntityMoveLook(cache, dx, dy, dz, yaw, pitch, false);
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(entity, convertYaw(yaw));

        sendPackets(packet, head);
    }

    public void show() {

        // Construct the packets out of the loop to save resources, they will
        // be the same for each Player.
        Packet<?> spawn = type.isAlive()
                ? new PacketPlayOutSpawnEntityLiving((EntityLiving) entity)
                : new PacketPlayOutSpawnEntity(entity, getSpawnId(), type == EntityType.FALLING_BLOCK ? Block.getCombinedId(block) : 0);
        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(cache, entity.getDataWatcher(), true);
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(entity, convertYaw(getYaw()));
        PacketPlayOutEntityLook look = new PacketPlayOutEntityLook(cache, convertYaw(getYaw()), convertPitch(getPitch()), false);
        PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(cache, motion.getX(), motion.getY(), motion.getZ());


        for (Player temp : DistanceUtil.getPlayersInRange(location)) {
            PlayerConnection connection = ((CraftPlayer) temp).getHandle().playerConnection;
            if (connections.contains(connection)) {
                continue;
            }

            connection.sendPacket(spawn);
            connection.sendPacket(meta);
            connection.sendPacket(head);
            connection.sendPacket(velocity);
            connection.sendPacket(look);
            PacketPlayOutEntityEquipment[] equipment = getEquipmentPacket();
            if (equipment != null) {
                for (PacketPlayOutEntityEquipment packet : equipment) {
                    connection.sendPacket(packet);
                }
            }

            connections.add(connection);
        }
    }

    @Override
    public void show(@NotNull Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        if (connections.contains(connection))
            throw new IllegalArgumentException();

        connection.sendPacket(type.isAlive()
                ? new PacketPlayOutSpawnEntityLiving((EntityLiving) entity)
                : new PacketPlayOutSpawnEntity(entity, getSpawnId(), type == EntityType.FALLING_BLOCK ? Block.getCombinedId(block) : 0));
        connection.sendPacket(new PacketPlayOutEntityMetadata(cache, entity.getDataWatcher(), true));
        connection.sendPacket(new PacketPlayOutEntityLook(cache, convertYaw(getYaw()), convertPitch(getPitch()), false));
        connection.sendPacket(new PacketPlayOutEntityVelocity(cache, motion.getX(), motion.getY(), motion.getZ()));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(entity, convertYaw(getYaw())));
        PacketPlayOutEntityEquipment[] equipment = getEquipmentPacket();
        if (equipment != null) {
            for (PacketPlayOutEntityEquipment packet : equipment) {
                connection.sendPacket(packet);
            }
        }

        // Inject the player's packet connection into this listener, so we can
        // show the player position/velocity/rotation changes
        connections.add(connection);
    }

    @Override
    public void updateMeta() {
        if (type == EntityType.ARMOR_STAND) ((EntityArmorStand) entity).setHeadPose(new Vector3f(getPitch(), 0, 0));

        sendPackets(new PacketPlayOutEntityMetadata(cache, entity.getDataWatcher(), false));
    }

    @Override
    public void remove() {
        sendPackets(new PacketPlayOutEntityDestroy(cache));
        connections.clear();
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

    @Override
    public void playEffect(EntityEffect effect) {
        if (!effect.getApplicable().isAssignableFrom(type.getEntityClass())) return;
        sendPackets(new PacketPlayOutEntityStatus(entity, effect.getData()));
    }

    @Override
    public void setEquipment(@NotNull org.bukkit.inventory.EquipmentSlot equipmentSlot, org.bukkit.inventory.ItemStack itemStack) {
        if (!type.isAlive())
            throw new IllegalStateException("Cannot set equipment of " + type);

        EnumItemSlot slot = switch (equipmentSlot) {
            case HAND -> EnumItemSlot.MAINHAND;
            case OFF_HAND -> EnumItemSlot.OFFHAND;
            case FEET -> EnumItemSlot.FEET;
            case CHEST -> EnumItemSlot.CHEST;
            case LEGS -> EnumItemSlot.LEGS;
            case HEAD -> EnumItemSlot.HEAD;
        };

        EntityLiving livingEntity = (EntityLiving) entity;
        livingEntity.setSlot(slot, CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public void updateEquipment() {
        PacketPlayOutEntityEquipment[] packet = getEquipmentPacket();
        if (packet != null) sendPackets(packet);
    }

    private PacketPlayOutEntityEquipment[] getEquipmentPacket() {
        if (!type.isAlive()) return null;
        EntityLiving livingEntity = (EntityLiving) entity;

        List<PacketPlayOutEntityEquipment> temp = new ArrayList<>(SLOTS.length);
        for (EnumItemSlot slot : SLOTS) {
            ItemStack item = livingEntity.getEquipment(slot);

            if (item != null && !item.isEmpty()) {
                temp.add(new PacketPlayOutEntityEquipment(cache, slot, item));
            }
        }

        return temp.isEmpty() ? null : temp.toArray(new PacketPlayOutEntityEquipment[0]);
    }

    private void sendPackets(Packet<?>... packets) {
        Iterator<PlayerConnection> connectionIterator = connections.iterator();
        while (connectionIterator.hasNext()) {
            PlayerConnection connection = connectionIterator.next();
            if (connection.isDisconnected()) {
                connectionIterator.remove();
                continue;
            }
            for (Packet<?> packet : packets) {
                connection.sendPacket(packet);
            }
        }
    }
}
