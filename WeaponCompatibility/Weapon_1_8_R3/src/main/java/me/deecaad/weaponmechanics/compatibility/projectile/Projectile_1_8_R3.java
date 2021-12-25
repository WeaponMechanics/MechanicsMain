package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileSettings;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Projectile_1_8_R3 implements IProjectileCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 8) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Projectile_1_8_R3.class + " when not using Minecraft 8",
                    new InternalError()
            );
        }
    }

    @Override
    public void disguise(AProjectile projectile) {
        ProjectileSettings projectileSettings = projectile.getProjectileSettings();
        EntityType type = projectileSettings.getProjectileDisguise();
        if (type == null) return;

        // Spawn
        Vector location = projectile.getLocation();
        World world = projectile.getWorld();
        Location bukkitLocation = location.toLocation(world);
        Vector normalizedMotion = projectile.getNormalizedMotion();
        float yaw = calculateYaw(normalizedMotion);
        float pitch = calculatePitch(normalizedMotion);
        Entity entity;

        switch (type) {
            case FALLING_BLOCK:
                Block nmsBlock = CraftMagicNumbers.getBlock(projectileSettings.getDisguiseItemOrBlock().getType());
                IBlockData nmsIBlockData = nmsBlock.getBlockData();

                entity = new EntityFallingBlock(((CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsIBlockData);

                PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(entity, Block.getCombinedId(nmsIBlockData));
                PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(entity, convertYawToByte(type, yaw));

                DistanceUtil.sendPacket(bukkitLocation, spawn, headRotation);
                break;
            case DROPPED_ITEM:
                ItemStack nmsStack = CraftItemStack.asNMSCopy(projectile.getProjectileSettings().getDisguiseItemOrBlock());
                entity = new EntityItem(((CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsStack);

                spawn = new PacketPlayOutSpawnEntity(entity, 1);
                PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), false);
                headRotation = new PacketPlayOutEntityHeadRotation(entity, convertYawToByte(type, yaw));

                DistanceUtil.sendPacket(bukkitLocation, spawn, metadata, headRotation);
                break;
            default:
                bukkitLocation.setYaw(yaw);
                bukkitLocation.setPitch(pitch);
                entity = ((CraftWorld) world).createEntity(bukkitLocation, type.getEntityClass());

                headRotation = new PacketPlayOutEntityHeadRotation(entity, convertYawToByte(type, yaw));
                if (type.isAlive()) {
                    PacketPlayOutSpawnEntityLiving spawnLiving = new PacketPlayOutSpawnEntityLiving((EntityLiving) entity);
                    DistanceUtil.sendPacket(bukkitLocation, spawnLiving, headRotation);
                } else {
                    spawn = new PacketPlayOutSpawnEntity(entity, 1);
                    DistanceUtil.sendPacket(bukkitLocation, spawn, headRotation);
                }
                break;
        }

        // Update once here since runnable is called 1 tick late
        update(projectile, entity);

        // Update task every tick
        new BukkitRunnable() {
            public void run() {

                // If its marked for removal, cancel task and destroy disguise
                if (projectile.isDead()) {
                    DistanceUtil.sendPacket(projectile.getLocation().toLocation(projectile.getWorld()), new PacketPlayOutEntityDestroy(entity.getId()));
                    cancel();
                    return;
                }

                update(projectile, entity);
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, 0);
    }

    private void update(AProjectile projectile, Entity entity) {
        Vector location = projectile.getLocation();
        Location bukkitLocation = location.toLocation(projectile.getWorld());
        Vector motion = projectile.getMotion();
        Vector normalizedMotion = projectile.getNormalizedMotion();
        int entityId = entity.getId();
        float yaw = calculateYaw(normalizedMotion);
        float pitch = calculatePitch(normalizedMotion);

        PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(entityId, motion.getX(), motion.getY(), motion.getZ());

        // https://wiki.vg/Data_types#Fixed-point_numbers
        double motionLength = projectile.getMotionLength();
        if (motionLength > 4 || NumberUtil.equals(motionLength, 0.0)) {
            entity.locX = location.getX();
            entity.locY = location.getY();
            entity.locZ = location.getZ();
            entity.yaw = yaw;
            entity.pitch = pitch;

            PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(entity);
            DistanceUtil.sendPacket(bukkitLocation, velocity, teleport);
        } else {
            Vector lastLocation = projectile.getLastLocation();
            EntityType type = projectile.getProjectileSettings().getProjectileDisguise();

            byte x = (byte) floor((location.getX() - lastLocation.getX()) * 32);
            byte y = (byte) floor((location.getY() - lastLocation.getY()) * 32);
            byte z = (byte) floor((location.getZ() - lastLocation.getZ()) * 32);

            PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook moveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(entityId, x, y, z, convertYawToByte(type, yaw), convertPitchToByte(type, pitch), false);
            DistanceUtil.sendPacket(bukkitLocation, velocity, moveLook);
        }
    }

    private int floor(double toFloor) {
        int flooredValue = (int) toFloor;
        return toFloor < (double) flooredValue ? flooredValue - 1 : flooredValue;
    }

    @Override
    public HitBox getHitBox(org.bukkit.entity.Entity entity) {
        if (!entity.getType().isAlive() || entity.isDead()) return null;

        AxisAlignedBB aabb = ((CraftEntity) entity).getHandle().getBoundingBox();
        HitBox hitBox = new HitBox(aabb.a, aabb.b, aabb.c, aabb.d, aabb.e, aabb.f);
        hitBox.setLivingEntity((LivingEntity) entity);
        return hitBox;
    }

    @Override
    public HitBox getHitBox(org.bukkit.block.Block block) {
        if (block.isEmpty() || block.isLiquid()) return null;

        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = worldServer.getType(blockPosition);
        Block nmsBlock = blockData.getBlock();
        nmsBlock.updateShape(worldServer, blockPosition);

        // Passable block check -> false means passable (thats why !)
        if (!(nmsBlock.a(worldServer, blockPosition, blockData) != null && nmsBlock.a(blockData, false))) return null;

        int x = blockPosition.getX(), y = blockPosition.getY(), z = blockPosition.getZ();
        HitBox hitBox = new HitBox(x + nmsBlock.B(), y + nmsBlock.D(), z + nmsBlock.F(), x + nmsBlock.C(), y + nmsBlock.E(), z + nmsBlock.G());
        hitBox.setBlockHitBox(block);
        return hitBox;
    }
}