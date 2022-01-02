package me.deecaad.core.compatibility.entity;

import me.deecaad.core.compatibility.ICompatibility;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import me.deecaad.core.compatibility.equipevent.NonNullList_1_17_R1;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
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

public class Entity_1_17_R1 implements EntityCompatibility {

    private static final Class<?> metaPacketClass;
    private static final Field metaPacketB;

    static {
        metaPacketClass = ReflectionUtil.getPacketClass("PacketPlayOutEntityMetadata");
        metaPacketB = ReflectionUtil.getField(metaPacketClass, "b");

        if (ReflectionUtil.getMCVersion() != 17) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Entity_1_17_R1.class + " when not using Minecraft 17",
                    new InternalError()
            );
        }
    }

    @Override
    public @NotNull Object getNMSEntity(org.bukkit.entity.@NotNull Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    public int getId(org.bukkit.entity.@NotNull Entity entity) {
        return ((CraftEntity) entity).getHandle().getId();
    }

    @Override
    public @NotNull Object getSpawnPacket(@NotNull Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_17_R1 Entity!");
        }

        if (entity instanceof FallingBlockEntity) {
            FallingBlockEntity block = (FallingBlockEntity) entity;
            return new ClientboundAddEntityPacket(block, Block.getId(block.getBlockState()));
        }

        return new ClientboundAddEntityPacket((Entity) entity);
    }

    @Override
    public @NotNull Object getVelocityPacket(@NotNull Object entity, Vector velocity) {
        return new ClientboundSetEntityMotionPacket(((Entity) entity).getId(),
                new Vec3(velocity.getX(), velocity.getY(), velocity.getZ()));
    }

    @Override
    public @NotNull Object getMetadataPacket(@NotNull Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_17_R1 Entity!");
        }

        Entity nmsEntity = (Entity) entity;
        return new ClientboundSetEntityDataPacket(nmsEntity.getId(), nmsEntity.getEntityData(), true);
    }

    @Override
    public @NotNull Object getMetadataPacket(@NotNull Object entity, boolean isEnableFlags, EntityMeta @NotNull ... flags) {

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
        SynchedEntityData dataWatcher = nmsEntity.getEntityData();
        List<SynchedEntityData.DataItem<?>> items = dataWatcher.getAll();

        // I don't think this should happen, at least not often. Make
        // sure to return some packet though
        if (items == null || items.isEmpty()) {
            return new ClientboundSetEntityDataPacket(nmsEntity.getId(), dataWatcher, true);
        }

        // Get the current byte data
        dataWatcher.clearDirty();
        @SuppressWarnings("unchecked")
        SynchedEntityData.DataItem<Byte> item = (SynchedEntityData.DataItem<Byte>) items.get(0);

        // Get the byte data, then apply the bitmask
        byte data = item.getValue();
        data = (byte) (isEnableFlags ? data | mask : data & ~mask);
        item.setValue(data);

        // Create the packet
        ClientboundSetEntityDataPacket metaPacket = new ClientboundSetEntityDataPacket(nmsEntity.getId(), dataWatcher, true);

        // I am taking the lazy way out and letting mojang set the fields then
        // I manually override them. The performance drop shouldn't be
        // significant enough to care about.
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
        List<SynchedEntityData.DataItem<?>> items = (List<SynchedEntityData.DataItem<?>>) ReflectionUtil.invokeField(metaPacketB, packet);

        @SuppressWarnings("unchecked")
        SynchedEntityData.DataItem<Byte> item = (SynchedEntityData.DataItem<Byte>) items.get(0);

        // Get the byte data, then apply the bitmask
        byte data = item.getValue();
        data = (byte) (isEnableFlags ? data | mask : data & ~mask);
        item.setValue(data);

        ReflectionUtil.setField(metaPacketB, packet, items);
        return packet;
    }

    @Override
    public @NotNull Object getDestroyPacket(@NotNull Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_17_R1 Entity!");
        }

        return new ClientboundRemoveEntitiesPacket(((Entity) entity).getId());
    }

    @Override
    public void spawnFirework(@NotNull Plugin plugin, Location loc, @NotNull Collection<? extends Player> players, byte flightTime, FireworkEffect @NotNull ...effects) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("Location#getWorld must not return null!");
        }

        // Instantiate the firework
        ServerLevel world = ((CraftWorld) loc.getWorld()).getHandle();
        FireworkRocketEntity fireworks = new FireworkRocketEntity(world, loc.getX(), loc.getY(), loc.getZ(), ItemStack.EMPTY);
        fireworks.lifetime = flightTime;

        // Handle fireworkeffects
        ItemStack item = new ItemStack(CraftMagicNumbers.getItem(org.bukkit.Material.FIREWORK_ROCKET));
        FireworkMeta meta = (FireworkMeta) CraftItemFactory.instance().getItemMeta(org.bukkit.Material.FIREWORK_ROCKET);
        meta.addEffects(effects);
        CraftItemStack.setItemMeta(item, meta);
        fireworks.getEntityData().set(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM, item);

        // Spawn in the firework for all given players
        ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(fireworks);
        ClientboundSetEntityDataPacket metaPacket = new ClientboundSetEntityDataPacket(fireworks.getId(), fireworks.getEntityData(), true);
        ICompatibility compatibility = CompatibilityAPI.getCompatibility();
        for (Player player : players) {
            compatibility.sendPackets(player, spawnPacket, metaPacket);
        }

        // Separate from the for loop to only schedule 1 task
        new BukkitRunnable() {
            @Override
            public void run() {

                // 17 is the status for firework explosion effect
                ClientboundEntityEventPacket statusPacket = new ClientboundEntityEventPacket(fireworks, (byte) 17);
                ClientboundRemoveEntitiesPacket destroyPacket = new ClientboundRemoveEntitiesPacket(fireworks.getId());
                for (Player player : players) {
                    compatibility.sendPackets(player, statusPacket, destroyPacket);
                }
            }
        }.runTaskLaterAsynchronously(plugin, flightTime);
    }

    @Override
    public @NotNull FallingBlockWrapper createFallingBlock(@NotNull Location loc, org.bukkit.Material mat, byte data, Vector motion, int maxTicks) {
        BlockState blockData = ((CraftBlockData) mat.createBlockData()).getState();
        return createFallingBlock(loc, blockData, motion, maxTicks);
    }

    @Override
    public @NotNull FallingBlockWrapper createFallingBlock(Location loc, @NotNull org.bukkit.block.BlockState state, Vector motion, int maxTicks) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("World cannot be null");
        }

        BlockState blockData = ((CraftBlockState) state).getHandle();
        return createFallingBlock(loc, blockData, motion, maxTicks);
    }

    private FallingBlockWrapper createFallingBlock(Location loc, BlockState data, Vector motion, int maxTicks) {
        ServerLevel world = ((CraftWorld) loc.getWorld()).getHandle();

        // Create an anonymous falling block implementation that simplifies movement logic
        // in order to calculate the amount of ticks the falling block will live for.
        FallingBlockEntity block = new FallingBlockEntity(world, loc.getX(), loc.getY(), loc.getZ(), data) {
            @Override
            public void tick() {
                time++;

                setDeltaMovement(getDeltaMovement().add(0.0, -0.04, 0.0));
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().scale(0.98));
            }

            @Override
            public void move(MoverType moveType, Vec3 motion) {

                // Some collision thing
                Vec3 vec = getCollisionVector(motion);
                if (vec.lengthSqr() > 1.0E-7) {
                    this.setPos(this.getX() + vec.x, this.getY() + vec.y, this.getZ() + vec.z);
                }

                this.horizontalCollision = !Mth.equal(motion.x, vec.x) || !Mth.equal(motion.z, vec.z);
                this.verticalCollision = motion.y != vec.y;
                this.onGround = this.verticalCollision && motion.y < 0.0D;

                if (isOnGround()) {
                    kill();
                    return;
                }

                double blockSpeed = getBlockSpeedFactor();
                setDeltaMovement(getDeltaMovement().multiply(blockSpeed, 1, blockSpeed));
            }

            /**
             * This will have to be changed for each version, copied from the nms entity
             * class (Called near the start of the move method)
             */
            @SuppressWarnings("unchecked")
            private Vec3 getCollisionVector(Vec3 vec3d) {
                return null;
            }
        };

        int ticksAlive = -1;

        // Only determine the ticks the block will live if the arguments
        // allow it
        if (motion != null && maxTicks > 0) {

            block.setDeltaMovement(motion.getX(), motion.getY(), motion.getZ());
            while (block.isAlive() && block.time < maxTicks) {
                block.tick();
            }

            ticksAlive = block.time;
        }

        // Create a new block since the previous one is dead. No need
        // to assign this block's motion, since that can only be updated
        // via Velocity packets.
        block = new FallingBlockEntity(world, loc.getX(), loc.getY(), loc.getZ(), data);

        return new FallingBlockWrapper(block, ticksAlive);
    }

    @Override
    public @NotNull Object toNMSItemEntity(org.bukkit.inventory.@NotNull ItemStack item, org.bukkit.@NotNull World world, double x, double y, double z) {
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        return new ItemEntity(nmsWorld, x, y, z, nmsItem);
    }

    @Override
    public List generateNonNullList(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        return new NonNullList_1_17_R1(size, consumer);
    }

    @Override
    public FakeEntity generateFakeEntity(Location location, EntityType type, Object data) {
        return null;
    }
}