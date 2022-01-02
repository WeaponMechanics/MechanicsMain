package me.deecaad.core.compatibility.entity;

import com.google.common.base.Optional;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.ICompatibility;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;


public class Entity_1_10_R1 implements EntityCompatibility {

    private static final Class<?> metaPacketClass;
    private static final Field metaPacketA;
    private static final Field metaPacketB;

    static {
        metaPacketClass = ReflectionUtil.getPacketClass("PacketPlayOutEntityMetadata");
        metaPacketA = ReflectionUtil.getField(metaPacketClass, "a");
        metaPacketB = ReflectionUtil.getField(metaPacketClass, "b");

        if (ReflectionUtil.getMCVersion() != 10) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Entity_1_10_R1.class + " when not using Minecraft 10",
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
            throw new IllegalArgumentException("Given Object must be 1_10_R1 Entity!");
        }

        if (entity instanceof EntityFallingBlock) {
            EntityFallingBlock block = (EntityFallingBlock) entity;
            return new PacketPlayOutSpawnEntity(block, Block.getCombinedId(block.getBlock()));
        }

        return new PacketPlayOutSpawnEntity((Entity) entity, 0);
    }

    @Override
    public @NotNull Object getVelocityPacket(@NotNull Object entity, Vector velocity) {
        return new PacketPlayOutEntityVelocity(((Entity) entity).getId(), velocity.getX(), velocity.getY(), velocity.getZ());
    }

    @Override
    public @NotNull Object getMetadataPacket(@NotNull Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_10_R1 Entity!");
        }

        Entity nmsEntity = (Entity) entity;
        return new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true);
    }

    @Override
    public @NotNull Object getMetadataPacket(@NotNull Object entity, boolean isEnableFlags, EntityMeta @NotNull ... flags) {

        // Make sure the given object is an entity
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_10_R1 Entity!");
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
            throw new IllegalArgumentException("Given Object must be 1_10_R1 Entity!");
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
        EntityFireworks fireworks = new EntityFireworks(world, loc.getX(), loc.getY(), loc.getZ(), null);
        fireworks.expectedLifespan = flightTime;

        // Handle fireworkeffects
        ItemStack item = new ItemStack(CraftMagicNumbers.getItem(Material.FIREWORK));
        FireworkMeta meta = (FireworkMeta) CraftItemFactory.instance().getItemMeta(Material.FIREWORK);
        meta.addEffects(effects);
        CraftItemStack.setItemMeta(item, meta);
        fireworks.getDataWatcher().set(EntityFireworks.FIREWORK_ITEM, Optional.of(item));

        // Spawn in the firework for all given players
        PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(fireworks, 0);
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
    public @NotNull FallingBlockWrapper createFallingBlock(@NotNull Location loc, Material mat, byte data, Vector motion, int maxTicks) {

        IBlockData blockData = Block.getByCombinedId(mat.getId() + (data << 12));
        return createFallingBlock(loc, blockData, motion, maxTicks);
    }

    @Override
    public @NotNull FallingBlockWrapper createFallingBlock(Location loc, @NotNull BlockState state, Vector motion, int maxTicks) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("World cannot be null");
        }

        MaterialData data = state.getData();
        IBlockData blockData = Block.getByCombinedId(data.getItemType().getId() + data.getData() << 12);
        return createFallingBlock(loc, blockData, motion, maxTicks);
    }

    private FallingBlockWrapper createFallingBlock(Location loc, IBlockData data, Vector motion, int maxTicks) {
        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();

        // Create an anonymous falling block implementation that simplifies movement logic
        // in order to calculate the amount of ticks the falling block will live for.
        EntityFallingBlock block = new EntityFallingBlock(world, loc.getX(), loc.getY(), loc.getZ(), data) {
            @Override
            public void m() {
                ticksLived++;

                this.motY -= 0.04;

                // Ideally, this method is overridden. Since I don't have the
                // patience to sort through 300 lines of obfuscated code, I just
                // use this method. Performance is slightly worse, but I don't care
                // because nobody should be using this version anyway
                move(this.motX, this.motY, this.motZ);

                this.motX *= 0.98;
                this.motY *= 0.98;
                this.motZ *= 0.98;
            }

            @Override
            public boolean ai() {
                // This is to make sure this the move() method doesn't throw a bukkit event
                // when the falling block falls into lava
                return true;
            }
        };

        int ticksAlive = -1;

        // Only determine the ticks the block will live if the arguments
        // allow it
        if (motion != null && maxTicks > 0) {

            block.motX = motion.getX();
            block.motY = motion.getY();
            block.motZ = motion.getZ();
            while (block.isAlive() && block.ticksLived < maxTicks) {
                block.m();
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
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        return new EntityItem(nmsWorld, x, y, z, nmsItem);
    }

    @Override
    public double getAbsorption(@NotNull LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle().getAbsorptionHearts();
    }

    @Override
    public void setAbsorption(@NotNull LivingEntity entity, double absorption) {
        ((CraftLivingEntity) entity).getHandle().setAbsorptionHearts((float) absorption);
    }

    @Override
    public List generateNonNullList(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        return null;
    }

    @Override
    public FakeEntity generateFakeEntity(Location location, EntityType type, Object data) {
        return null;
    }
}
