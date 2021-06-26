package me.deecaad.compatibility.entity;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.ICompatibility;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StreamAccumulator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static me.deecaad.core.MechanicsCore.debug;

public class Entity_1_17_R1 implements EntityCompatibility {

    private static final Class<?> metaPacketClass;
    private static final Field metaPacketB;

    static {
        metaPacketClass = ReflectionUtil.getPacketClass("PacketPlayOutEntityMetadata");
        metaPacketB = ReflectionUtil.getField(metaPacketClass, "b");
    }

    @Override
    public Object getNMSEntity(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    public int getId(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle().getId();
    }

    @Override
    public Object getSpawnPacket(Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_17_R1 Entity!");
        }

        if (entity instanceof EntityFallingBlock) {
            EntityFallingBlock block = (EntityFallingBlock) entity;
            return new PacketPlayOutSpawnEntity(block, Block.getCombinedId(block.getBlock()));
        }

        return new PacketPlayOutSpawnEntity((Entity) entity);
    }

    @Override
    public Object getVelocityPacket(Object entity, Vector velocity) {
        return new PacketPlayOutEntityVelocity(((Entity) entity).getId(),
                new Vec3D(velocity.getX(), velocity.getY(), velocity.getZ()));
    }

    @Override
    public Object getMetadataPacket(Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_17_R1 Entity!");
        }

        Entity nmsEntity = (Entity) entity;
        return new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true);
    }

    @Override
    public Object getMetadataPacket(Object entity, boolean isEnableFlags, EntityMeta... flags) {

        // Make sure the given object is an entity
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_17_R1 Entity!");
        }

        // Setup the byte data
        byte mask = 0;
        for (EntityMeta flag : flags) {
            mask |= flag.getMask();
        }

        // Get the metadata stored in the entity
        Entity nmsEntity = (Entity) entity;
        DataWatcher dataWatcher = nmsEntity.getDataWatcher();
        List<DataWatcher.Item<?>> items = dataWatcher.b();

        // I don't think this should happen, at least not often. Make
        // sure to return some packet though
        if (items == null || items.isEmpty()) {
            debug.debug("Entity " + entity + " does not have metadata");
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
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(nmsEntity.getId(), dataWatcher, true);

        // I am taking the lazy way out and letting mojang set the fields then
        // I manually override them. The performance drop shouldn't be
        // significant enough to care about.
        ReflectionUtil.setField(metaPacketB, metaPacket, items);

        return metaPacket;
    }

    @Override
    public Object setMetadata(Object packet, boolean isEnableFlags, EntityMeta... flags) {

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
    public Object getDestroyPacket(Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_17_R1 Entity!");
        }

        return new PacketPlayOutEntityDestroy(((Entity) entity).getId());
    }

    @Override
    public void spawnFirework(Location loc, Collection<? extends Player> players, byte flightTime, FireworkEffect...effects) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("Location#getWorld must not return null!");
        }

        // Instantiate the firework
        World world = ((CraftWorld) loc.getWorld()).getHandle();
        EntityFireworks fireworks = new EntityFireworks(world, loc.getX(), loc.getY(), loc.getZ(), ItemStack.b);
        fireworks.f = flightTime;

        // Handle fireworkeffects
        ItemStack item = new ItemStack(CraftMagicNumbers.getItem(org.bukkit.Material.FIREWORK_ROCKET));
        FireworkMeta meta = (FireworkMeta) CraftItemFactory.instance().getItemMeta(org.bukkit.Material.FIREWORK_ROCKET);
        meta.addEffects(effects);
        CraftItemStack.setItemMeta(item, meta);
        fireworks.getDataWatcher().set(EntityFireworks.b, item);

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
        }.runTaskLaterAsynchronously(MechanicsCore.getPlugin(), flightTime);
    }

    @Override
    public FallingBlockWrapper createFallingBlock(Location loc, org.bukkit.Material mat, byte data, Vector motion, int maxTicks) {
        IBlockData blockData = ((CraftBlockData) mat.createBlockData()).getState();
        return createFallingBlock(loc, blockData, motion, maxTicks);
    }

    @Override
    public FallingBlockWrapper createFallingBlock(Location loc, BlockState state, Vector motion, int maxTicks) {
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
                b++;

                setMot(getMot().add(0.0, -0.04, 0.0));
                move(EnumMoveType.a, getMot());
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

                this.A = !MathHelper.b(motion.b, vec.b) || !MathHelper.b(motion.d, vec.d);
                this.B = motion.c != vec.c; // B = verticalCollision
                this.z = this.B && motion.c < 0.0;

                if (isOnGround()) {
                    die();
                    return;
                }

                double blockSpeed = getBlockSpeedFactor();
                setMot(getMot().d(blockSpeed, 1, blockSpeed));
            }

            /**
             * This will have to be changed for each version, copied from the nms entity
             * class (Called near the start of the move method)
             */
            @SuppressWarnings("unchecked")
            private Vec3D getCollisionVector(Vec3D vec3d) {
                AxisAlignedBB axisalignedbb = this.getBoundingBox();// 973
                VoxelShapeCollision voxelshapecollision = VoxelShapeCollision.a(this);// 974
                VoxelShape voxelshape = this.t.getWorldBorder().c();// 975
                Stream<VoxelShape> stream = VoxelShapes.c(voxelshape, VoxelShapes.a(axisalignedbb.shrink(1.0E-7D)), OperatorBoolean.i) ? Stream.empty() : Stream.of(voxelshape);// 976
                Stream<VoxelShape> stream1 = this.t.c(this, axisalignedbb.b(vec3d), (entity) -> {// 977
                    return true;// 978
                });
                StreamAccumulator<VoxelShape> streamaccumulator = new StreamAccumulator(Stream.concat(stream1, stream));// 980
                Vec3D vec3d1 = vec3d.g() == 0.0D ? vec3d : a(this, vec3d, axisalignedbb, this.t, voxelshapecollision, streamaccumulator);// 981
                boolean flag = vec3d.b != vec3d1.b;// 982
                boolean flag1 = vec3d.c != vec3d1.c;// 983
                boolean flag2 = vec3d.d != vec3d1.d;// 984
                boolean flag3 = this.z || flag1 && vec3d.c < 0.0D;// 985
                if (this.O > 0.0F && flag3 && (flag || flag2)) {// 987
                    Vec3D vec3d2 = a(this, new Vec3D(vec3d.b, (double)this.O, vec3d.d), axisalignedbb, this.t, voxelshapecollision, streamaccumulator);// 988
                    Vec3D vec3d3 = a(this, new Vec3D(0.0D, (double)this.O, 0.0D), axisalignedbb.b(vec3d.b, 0.0D, vec3d.d), this.t, voxelshapecollision, streamaccumulator);// 989
                    if (vec3d3.c < (double)this.O) {// 991
                        Vec3D vec3d4 = a(this, new Vec3D(vec3d.b, 0.0D, vec3d.d), axisalignedbb.c(vec3d3), this.t, voxelshapecollision, streamaccumulator).e(vec3d3);// 992
                        if (vec3d4.i() > vec3d2.i()) {// 994
                            vec3d2 = vec3d4;// 995
                        }
                    }

                    if (vec3d2.i() > vec3d1.i()) {// 999
                        return vec3d2.e(a(this, new Vec3D(0.0D, -vec3d2.c + vec3d.c, 0.0D), axisalignedbb.c(vec3d2), this.t, voxelshapecollision, streamaccumulator));// 1000
                    }
                }

                return vec3d1;// 1004
            }
        };

        int ticksAlive = -1;

        // Only determine the ticks the block will live if the arguments
        // allow it
        if (motion != null && maxTicks > 0) {

            block.setMot(motion.getX(), motion.getY(), motion.getZ());
            while (block.isAlive() && block.b < maxTicks) {
                block.tick();
            }

            ticksAlive = block.b;
        }

        // Create a new block since the previous one is dead. No need
        // to assign this block's motion, since that can only be updated
        // via Velocity packets.
        block = new EntityFallingBlock(world, loc.getX(), loc.getY(), loc.getZ(), data);

        return new FallingBlockWrapper(block, ticksAlive);
    }

    @Override
    public Object toNMSItemEntity(org.bukkit.inventory.ItemStack item, org.bukkit.World world, double x, double y, double z) {
        World nmsWorld = ((CraftWorld) world).getHandle();
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        return new EntityItem(nmsWorld, x, y, z, nmsItem);
    }
}