package me.deecaad.compatibility.entity;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.ICompatibility;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static me.deecaad.core.MechanicsCore.debug;

public class Entity_1_12_R1 implements EntityCompatibility {

    private static final Class<?> metaPacketClass;
    private static final Field metaPacketA;
    private static final Field metaPacketB;

    static {
        metaPacketClass = ReflectionUtil.getNMSClass("PacketPlayOutEntityMetadata");
        metaPacketA = ReflectionUtil.getField(metaPacketClass, "a");
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
            throw new IllegalArgumentException("Given Object must be 1_12_R1 Entity!");
        }

        if (entity instanceof EntityFallingBlock) {
            EntityFallingBlock block = (EntityFallingBlock) entity;
            return new PacketPlayOutSpawnEntity(block, Block.getCombinedId(block.getBlock()));
        }

        return new PacketPlayOutSpawnEntity((Entity) entity, 0);
    }

    @Override
    public Object getVelocityPacket(Object entity, Vector velocity) {
        return new PacketPlayOutEntityVelocity(((Entity) entity).getId(), velocity.getX(), velocity.getY(), velocity.getZ());
    }

    @Override
    public Object getMetadataPacket(Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_12_R1 Entity!");
        }

        Entity nmsEntity = (Entity) entity;
        return new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true);
    }

    @Override
    public Object getMetadataPacket(Object entity, boolean isEnableFlags, EntityMeta... flags) {

        // Make sure the given object is an entity
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Given Object must be 1_12_R1 Entity!");
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
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();
        ReflectionUtil.setField(metaPacketA, metaPacket, nmsEntity.getId());
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
            throw new IllegalArgumentException("Given Object must be 1_12_R1 Entity!");
        }

        return new PacketPlayOutEntityDestroy(((Entity) entity).getId());
    }

    @Override
    public void spawnFirework(Location loc, Collection<? extends Player> players, byte flightTime, FireworkEffect... effects) {
        if (loc.getWorld() == null) {
            throw new IllegalArgumentException("Location#getWorld must not return null!");
        }

        // Instantiate the firework
        World world = ((CraftWorld) loc.getWorld()).getHandle();
        EntityFireworks fireworks = new EntityFireworks(world, loc.getX(), loc.getY(), loc.getZ(), ItemStack.a);
        fireworks.expectedLifespan = flightTime;

        // Handle fireworkeffects
        ItemStack item = new ItemStack(CraftMagicNumbers.getItem(Material.FIREWORK_ROCKET));
        FireworkMeta meta = (FireworkMeta) CraftItemFactory.instance().getItemMeta(Material.FIREWORK_ROCKET);
        meta.addEffects(effects);
        CraftItemStack.setItemMeta(item, meta);
        fireworks.getDataWatcher().set(EntityFireworks.FIREWORK_ITEM, item);

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
        }.runTaskLaterAsynchronously(MechanicsCore.getPlugin(), flightTime);
    }

    @Override
    public FallingBlockWrapper createFallingBlock(Location loc, Material mat, byte data, Vector motion, int maxTicks) {

        IBlockData blockData = Block.getByCombinedId(mat.getId() + (data << 12));
        return createFallingBlock(loc, blockData, motion, maxTicks);
    }

    @Override
    public FallingBlockWrapper createFallingBlock(Location loc, BlockState state, Vector motion, int maxTicks) {
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
        EntityFallingBlock block = new EntityFallingBlock(world, loc.getX(), loc.getBlockY(), loc.getZ(), data) {
            @Override
            public void B_() {
                ticksLived++;

                this.motY -= 0.04;

                // Ideally, this method is overridden. Since I don't have the
                // patience to sort through 300 lines of obfuscated code, I just
                // use this method. Performance is slightly worse, but I don't care
                // because nobody should be using this version anyway
                move(EnumMoveType.SELF, this.motX, this.motY, this.motZ);

                this.motX *= 0.98;
                this.motY *= 0.98;
                this.motZ *= 0.98;
            }

            @Override
            public boolean an() {
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
                block.B_();
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
    public Object toNMSItemEntity(org.bukkit.inventory.ItemStack item, org.bukkit.World world, double x, double y, double z) {
        World nmsWorld = ((CraftWorld) world).getHandle();
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        return new EntityItem(nmsWorld, x, y, z, nmsItem);
    }
}
