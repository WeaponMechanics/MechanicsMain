package me.deecaad.weaponmechanics.compatibility;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.compatibility.scope.Scope_1_12_R1;
import me.deecaad.weaponmechanics.compatibility.shoot.IShootCompatibility;
import me.deecaad.weaponmechanics.compatibility.shoot.Shoot_1_12_R1;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;

public class v1_12_R1 implements IWeaponCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 12) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + v1_12_R1.class + " when not using Minecraft 12",
                    new InternalError()
            );
        }
    }

    private final IScopeCompatibility scopeCompatibility;
    private final IShootCompatibility shootCompatibility;

    public v1_12_R1() {
        this.scopeCompatibility = new Scope_1_12_R1();
        this.shootCompatibility = new Shoot_1_12_R1();
    }

    @Nonnull
    @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility;
    }

    @Nonnull
    @Override
    public IShootCompatibility getShootCompatibility() {
        return shootCompatibility;
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

        AxisAlignedBB aabb = blockData.e(worldServer, blockPosition);
        // 1.12 -> e
        // 1.11 -> d
        // 1.9 - 1.10 -> c

        int x = blockPosition.getX(), y = blockPosition.getY(), z = blockPosition.getZ();
        HitBox hitBox = new HitBox(x + aabb.a, y + aabb.b, z + aabb.c, x + aabb.d, y + aabb.e, z + aabb.f);
        hitBox.setBlockHitBox(block);
        return hitBox;
    }
}