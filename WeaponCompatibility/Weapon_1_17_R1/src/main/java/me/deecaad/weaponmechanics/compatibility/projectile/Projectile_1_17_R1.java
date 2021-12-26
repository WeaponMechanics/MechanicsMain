package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileSettings;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Projectile_1_17_R1 implements IProjectileCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 17) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Projectile_1_17_R1.class + " when not using Minecraft 17",
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
                BlockState nmsIBlockData = nmsBlock.defaultBlockState();

                entity  = new FallingBlockEntity(((CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsIBlockData);

                ClientboundAddEntityPacket spawn = new ClientboundAddEntityPacket(entity, Block.getId(nmsIBlockData));
                ClientboundRotateHeadPacket headRotation = new ClientboundRotateHeadPacket(entity, convertYawToByte(type, yaw));

                DistanceUtil.sendPacket(bukkitLocation, spawn, headRotation);
                break;
            case DROPPED_ITEM:
                ItemStack nmsStack = CraftItemStack.asNMSCopy(projectile.getProjectileSettings().getDisguiseItemOrBlock());
                entity = new ItemEntity(((CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsStack);

                spawn = new ClientboundAddEntityPacket(entity, 1);
                ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), false);
                headRotation = new ClientboundRotateHeadPacket(entity, convertYawToByte(type, yaw));

                DistanceUtil.sendPacket(bukkitLocation, spawn, metadata, headRotation);
                break;
            default:
                bukkitLocation.setYaw(yaw);
                bukkitLocation.setPitch(pitch);
                entity = ((CraftWorld) world).createEntity(bukkitLocation, type.getEntityClass());

                headRotation = new ClientboundRotateHeadPacket(entity, convertYawToByte(type, yaw));
                if (type.isAlive()) {
                    ClientboundAddMobPacket spawnLiving = new ClientboundAddMobPacket((LivingEntity) entity);
                    DistanceUtil.sendPacket(bukkitLocation, spawnLiving, headRotation);
                } else {
                    spawn = new ClientboundAddEntityPacket(entity, 1);
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
                    DistanceUtil.sendPacket(projectile.getLocation().toLocation(projectile.getWorld()), new ClientboundRemoveEntitiesPacket(entity.getId()));
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

        ClientboundSetEntityMotionPacket velocity = new ClientboundSetEntityMotionPacket(entityId, new Vec3(motion.getX(), motion.getY(), motion.getZ()));

        double motionLength = projectile.getMotionLength();
        if (motionLength > 8.0 || NumberUtil.equals(motionLength, 0.0)) {
            entity.setPosRaw(location.getX(), location.getY(), location.getZ());
            entity.setXRot(yaw);
            entity.setYRot(pitch);

            ClientboundTeleportEntityPacket teleport = new ClientboundTeleportEntityPacket(entity);
            DistanceUtil.sendPacket(bukkitLocation, velocity, teleport);
        } else {
            Vector lastLocation = projectile.getLastLocation();
            EntityType type = projectile.getProjectileSettings().getProjectileDisguise();

            short x = (short) ((location.getX() * 32 - lastLocation.getX() * 32) * 128);
            short y = (short) ((location.getY() * 32 - lastLocation.getY() * 32) * 128);
            short z = (short) ((location.getZ() * 32 - lastLocation.getZ() * 32) * 128);

            ClientboundMoveEntityPacket.PosRot moveLook = new ClientboundMoveEntityPacket.PosRot(entityId, x, y, z, convertYawToByte(type, yaw), convertPitchToByte(type, pitch), false);
            DistanceUtil.sendPacket(bukkitLocation, velocity, moveLook);
        }
    }
}