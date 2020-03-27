package me.deecaad.weaponmechanics.compatibility.projectile;

import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import net.minecraft.server.v1_13_R1.AxisAlignedBB;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.VoxelShape;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Projectile_1_13_R1 implements IProjectileCompatibility {

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