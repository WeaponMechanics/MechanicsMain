package me.deecaad.weaponcompatibility.projectile;

import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import net.minecraft.server.v1_13_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Projectile_1_13_R1 implements IProjectileCompatibility {

    @Override
    public void spawnDisguise(CustomProjectile customProjectile, Vector location, Vector motion) {
        customProjectile.calculateYawAndPitch();

        World world = customProjectile.getWorld();
        float yaw = customProjectile.getProjectileDisguiseYaw();
        float pitch = customProjectile.getProjectileDisguisePitch();

        EntityType projectileDisguise = customProjectile.projectile.getProjectileDisguise();
        switch (projectileDisguise) {
            case FALLING_BLOCK:
                net.minecraft.server.v1_13_R1.Block nmsBlock = CraftMagicNumbers.getBlock(customProjectile.projectile.getProjectileStack().getType());
                IBlockData nmsIBlockData = nmsBlock.getBlockData();

                EntityFallingBlock nmsEntityFallingBlock = new EntityFallingBlock(((org.bukkit.craftbukkit.v1_13_R1.CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsIBlockData);
                customProjectile.setProjectileDisguiseId(nmsEntityFallingBlock.getId());

                PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(nmsEntityFallingBlock, net.minecraft.server.v1_13_R1.Block.getCombinedId(nmsIBlockData));
                PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(nmsEntityFallingBlock, convertYawToByte(customProjectile, yaw));

                sendUpdatePackets(customProjectile, 22500, spawn, headRotation);
                customProjectile.projectileDisguiseNMSEntity = nmsEntityFallingBlock;
                break;
            case DROPPED_ITEM:
                ItemStack nmsStack = CraftItemStack.asNMSCopy(customProjectile.projectile.getProjectileStack());

                EntityItem nmsEntityItem = new EntityItem(((org.bukkit.craftbukkit.v1_13_R1.CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsStack);
                customProjectile.setProjectileDisguiseId(nmsEntityItem.getId());

                spawn = new PacketPlayOutSpawnEntity(nmsEntityItem, 1);
                PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(customProjectile.getProjectileDisguiseId(), nmsEntityItem.getDataWatcher(), false);
                headRotation = new PacketPlayOutEntityHeadRotation(nmsEntityItem, convertYawToByte(customProjectile, yaw));

                sendUpdatePackets(customProjectile, 22500, spawn, metadata, headRotation);
                customProjectile.projectileDisguiseNMSEntity = nmsEntityItem;
                break;
            default:
                net.minecraft.server.v1_13_R1.Entity nmsEntity = ((org.bukkit.craftbukkit.v1_13_R1.CraftWorld) world).createEntity(location.toLocation(world, yaw, pitch), projectileDisguise.getEntityClass());
                customProjectile.setProjectileDisguiseId(nmsEntity.getId());

                headRotation = new PacketPlayOutEntityHeadRotation(nmsEntity, convertYawToByte(customProjectile, yaw));
                if (projectileDisguise.isAlive()) {
                    PacketPlayOutSpawnEntityLiving spawnLiving = new PacketPlayOutSpawnEntityLiving((EntityLiving) nmsEntity);
                    sendUpdatePackets(customProjectile, 22500, spawnLiving, headRotation);
                } else {
                    spawn = new PacketPlayOutSpawnEntity(nmsEntity, 1);
                    sendUpdatePackets(customProjectile, 22500, spawn, headRotation);
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
        if (motionLength > 8 || motionLength == 0) {
            net.minecraft.server.v1_13_R1.Entity nmsEntity = (net.minecraft.server.v1_13_R1.Entity) customProjectile.projectileDisguiseNMSEntity;

            nmsEntity.locX = location.getX();
            nmsEntity.locY = location.getY();
            nmsEntity.locZ = location.getZ();
            nmsEntity.yaw = yaw;
            nmsEntity.pitch = pitch;

            PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(nmsEntity);
            sendUpdatePackets(customProjectile, 8050, velocity, teleport);

        } else {
            short x = (short) ((location.getX() * 32 - lastLocation.getX() * 32) * 128);
            short y = (short) ((location.getY() * 32 - lastLocation.getY() * 32) * 128);
            short z = (short) ((location.getZ() * 32 - lastLocation.getZ() * 32) * 128);

            PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook moveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(projectileDisguiseId, x, y, z, convertYawToByte(customProjectile, yaw), convertPitchToByte(customProjectile, pitch), false);
            sendUpdatePackets(customProjectile, 8050, velocity, moveLook);
        }
    }

    @Override
    public void destroyDisguise(CustomProjectile customProjectile) {
        sendUpdatePackets(customProjectile, 22500, new PacketPlayOutEntityDestroy(customProjectile.getProjectileDisguiseId()));
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
        if (entity.isInvulnerable() || !entity.getType().isAlive() || entity.isDead()) return null;

        AxisAlignedBB aabb = ((CraftEntity) entity).getHandle().getBoundingBox();
        return new HitBox(new Vector(aabb.a, aabb.b, aabb.c), new Vector(aabb.d, aabb.e, aabb.f));
    }

    @Override
    public HitBox getHitBox(Block block) {
        if (block.isEmpty() || block.isLiquid()) return null;

        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();

        VoxelShape collisionShape = world.getType(blockPosition).h(world, blockPosition); // getCollisionShape
        if (collisionShape.b()) return null; // is empty, this basically means passable block

        VoxelShape shape = world.getType(blockPosition).g(world, blockPosition); // getShape
        if (shape.b()) return null; // is empty (might be air or something)

        AxisAlignedBB aabb = shape.a();

        int x = blockPosition.getX(), y = blockPosition.getY(), z = blockPosition.getZ();
        return new HitBox(new Vector(x + aabb.a, y + aabb.b, z + aabb.c),
                new Vector(x + aabb.d, y + aabb.e, z + aabb.f));
    }
}