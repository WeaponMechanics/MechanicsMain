package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Projectile_1_8_R3 implements IProjectileCompatibility {

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
        net.minecraft.server.v1_8_R3.Block nmsBlock = blockData.getBlock();
        nmsBlock.updateShape(worldServer, blockPosition);

        // Passable block check -> false means passable (thats why !)
        if (!(nmsBlock.a(worldServer, blockPosition, blockData) != null && nmsBlock.a(blockData, false))) return null;

        int x = blockPosition.getX(), y = blockPosition.getY(), z = blockPosition.getZ();
        return new HitBox(new Vector(x + nmsBlock.B(), y + nmsBlock.D(), z + nmsBlock.F()),
                new Vector(x + nmsBlock.C(), y + nmsBlock.E(), z + nmsBlock.G()));
    }
}