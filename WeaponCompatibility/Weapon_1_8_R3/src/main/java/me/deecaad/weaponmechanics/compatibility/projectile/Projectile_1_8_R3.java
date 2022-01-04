package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

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