package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.WorldServer;
import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Projectile_1_9_R1 implements IProjectileCompatibility {

    @Override
    public void spawnDisguise(CustomProjectile customProjectile) {

    }

    @Override
    public void updateDisguise(CustomProjectile customProjectile, float length) {

    }

    @Override
    public void destroyDisguise(CustomProjectile customProjectile) {

    }

    @Override
    public HitBox getHitBox(Entity entity) {
        if (entity.isInvulnerable()) return null;

        AxisAlignedBB aabb = ((CraftEntity) entity).getHandle().getBoundingBox();
        return new HitBox(new Vector(aabb.a, aabb.b, aabb.c), new Vector(aabb.d, aabb.e, aabb.f));
    }

    @Override
    public HitBox getHitBox(Block block) {
        if (block.isEmpty() || block.isLiquid()) return null;

        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = worldServer.getType(blockPosition);
        net.minecraft.server.v1_9_R1.Block nmsBlock = blockData.getBlock();

        // Passable block check -> false means passable (thats why !)
        if (!(blockData.d(worldServer, blockPosition) != net.minecraft.server.v1_9_R1.Block.k && nmsBlock.a(blockData, false))) return null;

        AxisAlignedBB aabb = blockData.c(worldServer, blockPosition);
        // 1.12 -> e
        // 1.11 -> d
        // 1.9 - 1.10 -> c

        int x = blockPosition.getX(), y = blockPosition.getY(), z = blockPosition.getZ();
        return new HitBox(new Vector(x + aabb.a, y + aabb.b, z + aabb.c),
                new Vector(x + aabb.d, y + aabb.e, z + aabb.f));
    }
}