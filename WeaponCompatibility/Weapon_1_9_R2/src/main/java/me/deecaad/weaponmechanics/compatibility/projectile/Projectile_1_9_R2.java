package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

public class Projectile_1_9_R2 implements IProjectileCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 9) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Projectile_1_9_R2.class + " when not using Minecraft 9",
                    new InternalError()
            );
        }
    }

    @Override
    public HitBox getHitBox(org.bukkit.entity.Entity entity) {
        if (entity.isInvulnerable() || !entity.getType().isAlive() || entity.isDead()) return null;

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

        // Passable block check -> false means passable (thats why !)
        if (!(blockData.d(worldServer, blockPosition) != Block.k && nmsBlock.a(blockData, false))) return null;

        AxisAlignedBB aabb = blockData.c(worldServer, blockPosition);
        // 1.12 -> e
        // 1.11 -> d
        // 1.9 - 1.10 -> c

        int x = blockPosition.getX(), y = blockPosition.getY(), z = blockPosition.getZ();
        HitBox hitBox = new HitBox(x + aabb.a, y + aabb.b, z + aabb.c, x + aabb.d, y + aabb.e, z + aabb.f);
        hitBox.setBlockHitBox(block);
        return hitBox;
    }
}
