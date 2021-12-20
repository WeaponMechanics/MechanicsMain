package me.deecaad.core.compatibility.entity;

import com.google.common.collect.ImmutableSet;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.ICompatibility;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import me.deecaad.core.compatibility.equipevent.NonNullList_1_15_R1;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class Entity_1_15_R1 implements EntityCompatibility {

    private static final Class<?> metaPacketClass;
    private static final Field metaPacketA;
    private static final Field metaPacketB;

    static {
        metaPacketClass = ReflectionUtil.getPacketClass("PacketPlayOutEntityMetadata");
        metaPacketA = ReflectionUtil.getField(metaPacketClass, "a");
        metaPacketB = ReflectionUtil.getField(metaPacketClass, "b");

        if (ReflectionUtil.getMCVersion() != 15) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Entity_1_15_R1.class + " when not using Minecraft 15",
                    new InternalError()
            );
        }
    }

    @Override
    public @NotNull Object getNMSEntity(org.bukkit.entity.@NotNull Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    @Override
    public int getId(org.bukkit.entity.@NotNull Entity entity) {
        return ((CraftEntity) entity).getHandle().getId();
    }

    @Override
    public @NotNull Object getSpawnPacket(@NotNull Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_15_R1 Entity!");
        }

        if (entity instanceof EntityFallingBlock) {
            EntityFallingBlock block = (EntityFallingBlock) entity;
            return new PacketPlayOutSpawnEntity(block, Block.getCombinedId(block.getBlock()));
        }

        return new PacketPlayOutSpawnEntity((Entity) entity);
    }

    @Override
    public @NotNull Object getVelocityPacket(@NotNull Object entity, Vector velocity) {
        return new PacketPlayOutEntityVelocity(((Entity) entity).getId(),
                new Vec3D(velocity.getX(), velocity.getY(), velocity.getZ()));
    }

    @Override
    public @NotNull Object getMetadataPacket(@NotNull Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_15_R1 Entity!");
        }

        Entity nmsEntity = (Entity) entity;
        return new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true);
    }

    @Override
    public @NotNull Object getMetadataPacket(@NotNull Object entity, boolean isEnableFlags, EntityMeta @NotNull ... flags) {

        // Make sure the given object is an entity
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_15_R1 Entity!");
        }

        // Setup the byte data
        byte mask = 0;
        for (EntityMeta flag : flags) {
            mask |= flag.getMask();
        }

        // Get the metadata stored in the entity
        Entity nmsEntity = (Entity) entity;
        DataWatcher dataWatcher = nmsEntity.getDataWatcher();
        List<DataWatcher.Item<?>> items = dataWatcher.c();

        // I don't think this should happen, at least not often. Make
        // sure to return some packet though
        if (items == null || items.isEmpty()) {
            return new PacketPlayOutEntityMetadata(nmsEntity.getId(), dataWatcher, true);
        }

        // Get the current byte data
        dataWatcher.e();
        @SuppressWarnings("unchecked")
        DataWatcher.Item<Byte> item = (DataWatcher.Item<Byte>) items.get(0);

        // Get the byte data, then apply the bitmask
        byte data = item.b();
        data = (byte) (isEnableFlags ? data | mask : data & ~mask);
        item.a(data);

        // Create the packet
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();
        ReflectionUtil.setField(metaPacketA, metaPacket, nmsEntity.getId());
        ReflectionUtil.setField(metaPacketB, metaPacket, items);

        return metaPacket;
    }

    @Override
    public Object setMetadata(@NotNull Object packet, boolean isEnableFlags, EntityMeta... flags) {

        // Setup the byte data
        byte mask = 0;
        for (EntityMeta flag : flags) {
            mask |= flag.getMask();
        }

        @SuppressWarnings("unchecked")
        List<DataWatcher.Item<?>> items = (List<DataWatcher.Item<?>>) ReflectionUtil.invokeField(metaPacketB, packet);

        @SuppressWarnings("unchecked")
        DataWatcher.Item<Byte> item = (DataWatcher.Item<Byte>) items.get(0);

        // Get the byte data, then apply the bitmask
        byte data = item.b();
        data = (byte) (isEnableFlags ? data | mask : data & ~mask);
        item.a(data);

        ReflectionUtil.setField(metaPacketB, packet, items);
        return packet;
    }

    @Override
    public @NotNull Object getDestroyPacket(@NotNull Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_15_R1 Entity!");
        }

        return new PacketPlayOutEntityDestroy(((Entity) entity).getId());
    }

    @Override
    public void spawnFirework(@NotNull Plugin plugin, Location loc, @NotNull Collection<? extends Player> players, byte flightTime, FireworkEffect @NotNull ...effects) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("Location#getWorld must not return null!");
        }

        // Instantiate the firework
        World world = ((CraftWorld) loc.getWorld()).getHandle();
        EntityFireworks fireworks = new EntityFireworks(world, loc.getX(), loc.getY(), loc.getZ(), ItemStack.a);
        fireworks.expectedLifespan = flightTime;

        // Handle fireworkeffects
        ItemStack item = new ItemStack(CraftMagicNumbers.getItem(org.bukkit.Material.FIREWORK_ROCKET));
        FireworkMeta meta = (FireworkMeta) CraftItemFactory.instance().getItemMeta(org.bukkit.Material.FIREWORK_ROCKET);
        meta.addEffects(effects);
        CraftItemStack.setItemMeta(item, meta);
        fireworks.getDataWatcher().set(EntityFireworks.FIREWORK_ITEM, item);

        // Spawn in the firework for all given players
        PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(fireworks);
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(fireworks.getId(), fireworks.getDataWatcher(), true);
        ICompatibility compatibility = CompatibilityAPI.getCompatibility();
        for (Player player : players) {
            compatibility.sendPackets(player, spawnPacket, metaPacket);
        }

        // Separate from the for loop to only schedule 1 task
        new BukkitRunnable() {
            @Override
            public void run() {

                // 17 is the status for firework explosion effect
                PacketPlayOutEntityStatus statusPacket = new PacketPlayOutEntityStatus(fireworks, (byte) 17);
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(fireworks.getId());
                for (Player player : players) {
                    compatibility.sendPackets(player, statusPacket, destroyPacket);
                }
            }
        }.runTaskLaterAsynchronously(plugin, flightTime);
    }

    @Override
    public @NotNull FallingBlockWrapper createFallingBlock(@NotNull Location loc, org.bukkit.Material mat, byte data, Vector motion, int maxTicks) {

        IBlockData blockData = ((CraftBlockData) mat.createBlockData()).getState();
        return createFallingBlock(loc, blockData, motion, maxTicks);
    }

    @Override
    public @NotNull FallingBlockWrapper createFallingBlock(Location loc, org.bukkit.block.@NotNull BlockState state, Vector motion, int maxTicks) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("World cannot be null");
        }

        IBlockData blockData = ((CraftBlockState) state).getHandle();
        return createFallingBlock(loc, blockData, motion, maxTicks);
    }

    private FallingBlockWrapper createFallingBlock(Location loc, IBlockData data, Vector motion, int maxTicks) {
        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();

        // Create an anonymous falling block implementation that simplifies movement logic
        // in order to calculate the amount of ticks the falling block will live for.
        EntityFallingBlock block = new EntityFallingBlock(world, loc.getX(), loc.getY(), loc.getZ(), data) {
            @Override
            public void tick() {
                ticksLived++;

                setMot(getMot().add(0.0, -0.04, 0.0));
                move(EnumMoveType.SELF, getMot());
                setMot(getMot().a(0.98));
            }

            @Override
            public void move(EnumMoveType moveType, Vec3D motion) {

                // Some collision thing
                Vec3D vec = getCollisionVector(motion);
                if (vec.g() > 1.0E-7) {

                    // Add vector to the bounding box
                    a(getBoundingBox().b(vec));
                }

                this.positionChanged = !MathHelper.b(motion.x, vec.x) || !MathHelper.b(motion.z, vec.z);
                this.v = motion.y != vec.y; // v = verticalCollision
                this.onGround = this.v && motion.y < 0.0;

                if (onGround) {
                    die();
                    return;
                }

                this.w = positionChanged || v; // w = collision

                setMot(getMot().d(this.ai(), 1, this.ai()));
            }

            /**
             * This will have to be changed for each version, copied from the nms entity
             * class (Called near the start of the move method)
             */
            @SuppressWarnings("unchecked")
            private Vec3D getCollisionVector(Vec3D vec3d) {
                AxisAlignedBB axisalignedbb = this.getBoundingBox();
                VoxelShapeCollision voxelshapecollision = VoxelShapeCollision.a(this);
                VoxelShape voxelshape = this.world.getWorldBorder().a();
                Stream<VoxelShape> stream = VoxelShapes.c(voxelshape, VoxelShapes.a(axisalignedbb.shrink(1.0E-7D)), OperatorBoolean.AND) ? Stream.empty() : Stream.of(voxelshape);
                Stream<VoxelShape> stream1 = this.world.b(this, axisalignedbb.a(vec3d), ImmutableSet.of());
                StreamAccumulator<VoxelShape> streamaccumulator = new StreamAccumulator(Stream.concat(stream1, stream));
                Vec3D vec3d1 = vec3d.g() == 0.0D ? vec3d : a(this, vec3d, axisalignedbb, this.world, voxelshapecollision, streamaccumulator);
                boolean flag = vec3d.x != vec3d1.x;
                boolean flag1 = vec3d.y != vec3d1.y;
                boolean flag2 = vec3d.z != vec3d1.z;
                boolean flag3 = this.onGround || flag1 && vec3d.y < 0.0D;
                if (this.H > 0.0F && flag3 && (flag || flag2)) {
                    Vec3D vec3d2 = a(this, new Vec3D(vec3d.x, this.H, vec3d.z), axisalignedbb, this.world, voxelshapecollision, streamaccumulator);
                    Vec3D vec3d3 = a(this, new Vec3D(0.0D, this.H, 0.0D), axisalignedbb.b(vec3d.x, 0.0D, vec3d.z), this.world, voxelshapecollision, streamaccumulator);
                    if (vec3d3.y < (double)this.H) {
                        Vec3D vec3d4 = a(this, new Vec3D(vec3d.x, 0.0D, vec3d.z), axisalignedbb.b(vec3d3), this.world, voxelshapecollision, streamaccumulator).e(vec3d3);
                        if (b(vec3d4) > b(vec3d2)) {
                            vec3d2 = vec3d4;
                        }
                    }

                    if (b(vec3d2) > b(vec3d1)) {
                        return vec3d2.e(a(this, new Vec3D(0.0D, -vec3d2.y + vec3d.y, 0.0D), axisalignedbb.b(vec3d2), this.world, voxelshapecollision, streamaccumulator));
                    }
                }

                return vec3d1;
            }
        };

        int ticksAlive = -1;

        // Only determine the ticks the block will live if the arguments
        // allow it
        if (motion != null && maxTicks > 0) {

            block.setMot(motion.getX(), motion.getY(), motion.getZ());
            while (block.isAlive() && block.ticksLived < maxTicks) {
                block.tick();
            }

            ticksAlive = block.ticksLived;
        }

        // Create a new block since the previous one is dead. No need
        // to assign this block's motion, since that can only be updated
        // via Velocity packets.
        block = new EntityFallingBlock(world, loc.getX(), loc.getY(), loc.getZ(), data);

        return new FallingBlockWrapper(block, ticksAlive);
    }

    @Override
    public @NotNull Object toNMSItemEntity(org.bukkit.inventory.@NotNull ItemStack item, org.bukkit.@NotNull World world, double x, double y, double z) {
        World nmsWorld = ((CraftWorld) world).getHandle();
        net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        return new EntityItem(nmsWorld, x, y, z, nmsItem);
    }

    @Override
    public List generateNonNullList(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        return new NonNullList_1_15_R1(size, consumer);
    }
}