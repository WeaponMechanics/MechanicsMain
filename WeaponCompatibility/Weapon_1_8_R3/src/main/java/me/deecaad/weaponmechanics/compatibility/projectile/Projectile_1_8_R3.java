package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.compatibility.shoot.IShootCompatibility;
import me.deecaad.weaponmechanics.compatibility.v1_8_R3;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
    public void spawnDisguise(CustomProjectile customProjectile, Vector location, Vector motion) {
        customProjectile.calculateYawAndPitch();

        World world = customProjectile.getWorld();
        float yaw = customProjectile.getProjectileDisguiseYaw();
        float pitch = customProjectile.getProjectileDisguisePitch();

        EntityType projectileDisguise = customProjectile.projectile.getProjectileDisguise();
        switch (projectileDisguise) {
            case FALLING_BLOCK:
                net.minecraft.server.v1_8_R3.Block nmsBlock = CraftMagicNumbers.getBlock(customProjectile.projectile.getProjectileStack().getType());
                net.minecraft.server.v1_8_R3.IBlockData nmsIBlockData = nmsBlock.getBlockData();

                EntityFallingBlock nmsEntityFallingBlock = new EntityFallingBlock(((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsIBlockData);
                customProjectile.setProjectileDisguiseId(nmsEntityFallingBlock.getId());

                PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(nmsEntityFallingBlock, net.minecraft.server.v1_8_R3.Block.getCombinedId(nmsIBlockData));
                PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(nmsEntityFallingBlock, convertYawToByte(customProjectile, yaw));

                DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), spawn, headRotation);
                customProjectile.projectileDisguiseNMSEntity = nmsEntityFallingBlock;
                break;
            case DROPPED_ITEM:
                ItemStack nmsStack = CraftItemStack.asNMSCopy(customProjectile.projectile.getProjectileStack());

                EntityItem nmsEntityItem = new EntityItem(((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsStack);
                customProjectile.setProjectileDisguiseId(nmsEntityItem.getId());

                spawn = new PacketPlayOutSpawnEntity(nmsEntityItem, 1);
                PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(customProjectile.getProjectileDisguiseId(), nmsEntityItem.getDataWatcher(), false);
                headRotation = new PacketPlayOutEntityHeadRotation(nmsEntityItem, convertYawToByte(customProjectile, yaw));

                DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), spawn, metadata, headRotation);
                customProjectile.projectileDisguiseNMSEntity = nmsEntityItem;
                break;
            default:
                net.minecraft.server.v1_8_R3.Entity nmsEntity = ((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) world).createEntity(location.toLocation(world, yaw, pitch), projectileDisguise.getEntityClass());
                customProjectile.setProjectileDisguiseId(nmsEntity.getId());

                headRotation = new PacketPlayOutEntityHeadRotation(nmsEntity, convertYawToByte(customProjectile, yaw));
                if (projectileDisguise.isAlive()) {
                    PacketPlayOutSpawnEntityLiving spawnLiving = new PacketPlayOutSpawnEntityLiving((EntityLiving) nmsEntity);
                    DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), spawnLiving, headRotation);
                } else {
                    spawn = new PacketPlayOutSpawnEntity(nmsEntity, 1);
                    DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), spawn, headRotation);
                }
                customProjectile.projectileDisguiseNMSEntity = nmsEntity;
                break;
        }
        updateDisguise(customProjectile, location, motion, location);
    }

    @Override
    public void updateDisguise(CustomProjectile customProjectile, Vector location, Vector motion, Vector lastLocation) {

        // Calculate yaw and pitch before doing updates
        customProjectile.calculateYawAndPitch();

        int projectileDisguiseId = customProjectile.getProjectileDisguiseId();
        float yaw = customProjectile.getProjectileDisguiseYaw();
        float pitch = customProjectile.getProjectileDisguisePitch();

        PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(projectileDisguiseId, motion.getX(), motion.getY(), motion.getZ());

        double motionLength = customProjectile.getMotionLength();
        // https://wiki.vg/Data_types#Fixed-point_numbers

        if (motionLength > 4 || motionLength == 0) {
            net.minecraft.server.v1_8_R3.Entity nmsEntity = (net.minecraft.server.v1_8_R3.Entity) customProjectile.projectileDisguiseNMSEntity;

            nmsEntity.locX = location.getX();
            nmsEntity.locY = location.getY();
            nmsEntity.locZ = location.getZ();
            nmsEntity.yaw = yaw;
            nmsEntity.pitch = pitch;

            PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(nmsEntity);
            DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), velocity, teleport);

        } else {
            byte x = (byte) floor((location.getX() - lastLocation.getX()) * 32);
            byte y = (byte) floor((location.getY() - lastLocation.getY()) * 32);
            byte z = (byte) floor((location.getZ() - lastLocation.getZ()) * 32);

            PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook moveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(projectileDisguiseId, x, y, z, convertYawToByte(customProjectile, yaw), convertPitchToByte(customProjectile, pitch), false);
            DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), velocity, moveLook);
        }
    }

    @Override
    public void destroyDisguise(CustomProjectile customProjectile) {
        DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), new PacketPlayOutEntityDestroy(customProjectile.getProjectileDisguiseId()));
    }

    @Override
    public double[] getDefaultWidthAndHeight(EntityType entityType) {
        World world = Bukkit.getWorlds().get(0);
        Location location = new Location(world, 1, 100, 1);
        org.bukkit.entity.Entity entity = ((CraftWorld) world).createEntity(location, entityType.getEntityClass()).getBukkitEntity();
        IShootCompatibility shootCompatibility = WeaponCompatibilityAPI.getShootCompatibility();
        return new double[]{ shootCompatibility.getWidth(entity), shootCompatibility.getHeight(entity) };
    }

    @Override
    public HitBox getHitBox(Entity entity) {
        if (!entity.getType().isAlive() || entity.isDead()) return null;

        AxisAlignedBB aabb = ((CraftEntity) entity).getHandle().getBoundingBox();
        return new HitBox(new Vector(aabb.a, aabb.b, aabb.c), new Vector(aabb.d, aabb.e, aabb.f));
    }

    @Override
    public HitBox getHitBox(Block block) {
        if (block.isEmpty() || block.isLiquid()) return null;

        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = worldServer.getType(blockPosition);
        net.minecraft.server.v1_8_R3.Block nmsBlock = blockData.getBlock();
        nmsBlock.updateShape(worldServer, blockPosition);

        // Passable block check -> false means passable (thats why !)
        if (!(nmsBlock.a(worldServer, blockPosition, blockData) != null && nmsBlock.a(blockData, false))) return null;

        int x = blockPosition.getX(), y = blockPosition.getY(), z = blockPosition.getZ();
        return new HitBox(new Vector(x + nmsBlock.B(), y + nmsBlock.D(), z + nmsBlock.F()),
                new Vector(x + nmsBlock.C(), y + nmsBlock.E(), z + nmsBlock.G()));
    }
}