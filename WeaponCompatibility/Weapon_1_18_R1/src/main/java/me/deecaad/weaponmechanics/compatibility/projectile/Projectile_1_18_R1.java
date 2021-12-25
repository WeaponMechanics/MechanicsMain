package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.compatibility.shoot.IShootCompatibility;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Projectile_1_18_R1 implements IProjectileCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 18) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Projectile_1_18_R1.class + " when not using Minecraft 18",
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
                Block nmsBlock = CraftMagicNumbers.getBlock(customProjectile.projectile.getProjectileStack().getType());
                BlockState nmsIBlockData = nmsBlock.defaultBlockState();

                FallingBlockEntity nmsEntityFallingBlock = new FallingBlockEntity(((org.bukkit.craftbukkit.v1_18_R1.CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsIBlockData);
                customProjectile.setProjectileDisguiseId(nmsEntityFallingBlock.getId());

                ClientboundAddEntityPacket spawn = new ClientboundAddEntityPacket(nmsEntityFallingBlock, Block.getId(nmsIBlockData));
                ClientboundRotateHeadPacket headRotation = new ClientboundRotateHeadPacket(nmsEntityFallingBlock, convertYawToByte(customProjectile, yaw));

                DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), spawn, headRotation);
                customProjectile.projectileDisguiseNMSEntity = nmsEntityFallingBlock;
                break;
            case DROPPED_ITEM:
                ItemStack nmsStack = CraftItemStack.asNMSCopy(customProjectile.projectile.getProjectileStack());

                ItemEntity nmsEntityItem = new ItemEntity(((org.bukkit.craftbukkit.v1_18_R1.CraftWorld) world).getHandle(), location.getX(), location.getY(), location.getZ(), nmsStack);
                customProjectile.setProjectileDisguiseId(nmsEntityItem.getId());

                spawn = new ClientboundAddEntityPacket(nmsEntityItem, 1);
                ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(customProjectile.getProjectileDisguiseId(), nmsEntityItem.getEntityData(), false);
                headRotation = new ClientboundRotateHeadPacket(nmsEntityItem, convertYawToByte(customProjectile, yaw));

                DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), spawn, metadata, headRotation);
                customProjectile.projectileDisguiseNMSEntity = nmsEntityItem;
                break;
            default:
                Entity nmsEntity = ((org.bukkit.craftbukkit.v1_18_R1.CraftWorld) world).createEntity(location.toLocation(world, yaw, pitch), projectileDisguise.getEntityClass());
                customProjectile.setProjectileDisguiseId(nmsEntity.getId());

                headRotation = new ClientboundRotateHeadPacket(nmsEntity, convertYawToByte(customProjectile, yaw));
                if (projectileDisguise.isAlive()) {
                    ClientboundAddMobPacket spawnLiving = new ClientboundAddMobPacket((LivingEntity) nmsEntity);
                    DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), spawnLiving, headRotation);
                } else {
                    spawn = new ClientboundAddEntityPacket(nmsEntity, 1);
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

        ClientboundSetEntityMotionPacket velocity = new ClientboundSetEntityMotionPacket(projectileDisguiseId, new Vec3(motion.getX(), motion.getY(), motion.getZ()));

        double motionLength = customProjectile.getMotionLength();
        if (motionLength > 8 || motionLength == 0) {
            Entity nmsEntity = (Entity) customProjectile.projectileDisguiseNMSEntity;

            nmsEntity.setPosRaw(location.getX(), location.getY(), location.getZ());
            nmsEntity.setXRot(yaw);
            nmsEntity.setYRot(pitch);

            ClientboundTeleportEntityPacket teleport = new ClientboundTeleportEntityPacket(nmsEntity);
            DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), velocity, teleport);

        } else {
            short x = (short) ((location.getX() * 32 - lastLocation.getX() * 32) * 128);
            short y = (short) ((location.getY() * 32 - lastLocation.getY() * 32) * 128);
            short z = (short) ((location.getZ() * 32 - lastLocation.getZ() * 32) * 128);

            ClientboundMoveEntityPacket.PosRot moveLook = new ClientboundMoveEntityPacket.PosRot(projectileDisguiseId, x, y, z, convertYawToByte(customProjectile, yaw), convertPitchToByte(customProjectile, pitch), false);
            DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), velocity, moveLook);
        }
    }

    @Override
    public void destroyDisguise(CustomProjectile customProjectile) {
        DistanceUtil.sendPacket(customProjectile.getBukkitLocation(), new ClientboundRemoveEntitiesPacket(customProjectile.getProjectileDisguiseId()));
    }

    @Override
    public double[] getDefaultWidthAndHeight(EntityType entityType) {
        World world = Bukkit.getWorlds().get(0);
        Location location = new Location(world, 1, 100, 1);
        org.bukkit.entity.Entity entity = ((CraftWorld) world).createEntity(location, entityType.getEntityClass()).getBukkitEntity();
        IShootCompatibility shootCompatibility = WeaponCompatibilityAPI.getShootCompatibility();
        return new double[]{ shootCompatibility.getWidth(entity), shootCompatibility.getHeight(entity) };
    }
}