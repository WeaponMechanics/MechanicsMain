package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.weaponmechanics.compatibility.shoot.IShootCompatibility;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Projectile_1_15_R1 implements IProjectileCompatibility {

    @Override
    public void spawnDisguise(CustomProjectile customProjectile) {
        calculateYawAndPitch(customProjectile);

        World world = customProjectile.world;
        Vector motion = customProjectile.motion;
        Vector location = customProjectile.location;
        float yaw = customProjectile.yaw;
        float pitch = customProjectile.pitch;

        EntityType projectileDisguise = customProjectile.projectile.getProjectileDisguise();
        switch (projectileDisguise) {
            case FALLING_BLOCK:
                Block nmsBlock = CraftMagicNumbers.getBlock(customProjectile.projectile.getProjectileStack().getType());
                IBlockData nmsIBlockData = nmsBlock.getBlockData();

                EntityFallingBlock nmsEntityFallingBlock = new EntityFallingBlock(((CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsIBlockData);
                customProjectile.projectileDisguiseId = nmsEntityFallingBlock.getId();

                PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(nmsEntityFallingBlock, Block.getCombinedId(nmsIBlockData));
                PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(nmsEntityFallingBlock, convertYawToByte(customProjectile, yaw));

                sendUpdatePackets(customProjectile, 22500, spawn, headRotation);
                customProjectile.nmsEntity = nmsEntityFallingBlock;
                break;
            case DROPPED_ITEM:
                ItemStack nmsStack = CraftItemStack.asNMSCopy(customProjectile.projectile.getProjectileStack());

                EntityItem nmsEntityItem = new EntityItem(((CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsStack);
                customProjectile.projectileDisguiseId = nmsEntityItem.getId();

                spawn = new PacketPlayOutSpawnEntity(nmsEntityItem, 1);
                PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(customProjectile.projectileDisguiseId, nmsEntityItem.getDataWatcher(), false);
                headRotation = new PacketPlayOutEntityHeadRotation(nmsEntityItem, convertYawToByte(customProjectile, yaw));

                sendUpdatePackets(customProjectile, 22500, spawn, metadata, headRotation);
                customProjectile.nmsEntity = nmsEntityItem;
                break;
            default:
                Entity nmsEntity = ((CraftWorld) world).createEntity(location.toLocation(world, yaw, pitch), projectileDisguise.getEntityClass());
                customProjectile.projectileDisguiseId = nmsEntity.getId();

                headRotation = new PacketPlayOutEntityHeadRotation(nmsEntity, convertYawToByte(customProjectile, yaw));
                if (projectileDisguise.isAlive()) {
                    PacketPlayOutSpawnEntityLiving spawnLiving = new PacketPlayOutSpawnEntityLiving((EntityLiving) nmsEntity);
                    sendUpdatePackets(customProjectile, 22500, spawnLiving, headRotation);
                } else {
                    spawn = new PacketPlayOutSpawnEntity(nmsEntity, 1);
                    sendUpdatePackets(customProjectile, 22500, spawn, headRotation);
                }
                customProjectile.nmsEntity = nmsEntity;
                break;
        }

        float length = (float) motion.length();
        updateDisguise(customProjectile, length);
    }

    @Override
    public void updateDisguise(CustomProjectile customProjectile, float length) {
        int projectileDisguiseId = customProjectile.projectileDisguiseId;
        Vector motion = customProjectile.motion;
        Vector location = customProjectile.location;
        Vector lastLocation = customProjectile.lastLocation;
        float yaw = customProjectile.yaw;
        float pitch = customProjectile.pitch;

        PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(projectileDisguiseId, new Vec3D(motion.getX(), motion.getY(), motion.getZ()));

        if (length > 8) {
            Entity nmsEntity = (Entity) customProjectile.nmsEntity;

            nmsEntity.setPositionRaw(location.getX(), location.getY(), location.getZ());
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
        sendUpdatePackets(customProjectile, 22500, new PacketPlayOutEntityDestroy(customProjectile.projectileDisguiseId));
    }

    @Override
    public double[] getDefaultWidthAndHeight(EntityType entityType) {
        World world = Bukkit.getWorlds().get(0);
        Location location = new Location(world, 1, 100, 1);
        org.bukkit.entity.Entity entity = ((CraftWorld) world).createEntity(location, entityType.getEntityClass()).getBukkitEntity();
        IShootCompatibility shootCompatibility = CompatibilityAPI.getCompatibility().getShootCompatibility();
        return new double[]{ shootCompatibility.getWidth(entity), shootCompatibility.getHeight(entity) };
    }
}