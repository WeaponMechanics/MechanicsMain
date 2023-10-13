package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.ray.BlockTraceResult;
import me.deecaad.core.utils.ray.EntityTraceResult;
import me.deecaad.core.utils.ray.RayTraceResult;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import org.jetbrains.annotations.Nullable;

public class StickedData {

    private Location blockLocation;
    private LivingEntity livingEntity;
    private final Vector relativeLocation;
    private final String worldName;

    public StickedData(RayTraceResult hit) {
        if (hit instanceof BlockTraceResult blockHit) {
            blockLocation = blockHit.getBlock().getLocation();
            relativeLocation = hit.getHitLocation().clone().subtract(blockLocation.toVector());
            worldName = blockLocation.getWorld().getName();
        } else if (hit instanceof EntityTraceResult entityHit) {
            livingEntity = entityHit.getEntity();
            relativeLocation = hit.getHitLocation().clone().subtract(livingEntity.getLocation().toVector());
            worldName = livingEntity.getWorld().getName();
        } else {
            throw new IllegalArgumentException("RayTraceResult is not BlockTraceResult or EntityTraceResult");
        }
    }

    public StickedData(WeaponProjectile projectile, Block block) {
        blockLocation = block.getLocation();
        relativeLocation = projectile.getLocation().add(new Vector(0, 0.05, 0)).subtract(blockLocation.toVector());
        worldName = blockLocation.getWorld().getName();
    }

    public Vector getNewLocation() {
        if (livingEntity != null) {
            return livingEntity.isDead() || !worldName.equals(livingEntity.getWorld().getName()) ? null : livingEntity.getLocation().clone().add(relativeLocation).toVector();
        }
        return CompatibilityAPI.getBlockCompatibility().getHitBox(blockLocation.getBlock()) == null ? null : blockLocation.clone().add(relativeLocation).toVector();
    }

    public boolean isBlockStick() {
        return blockLocation != null;
    }

    @Nullable
    public LivingEntity getLivingEntity() {
        return livingEntity == null || livingEntity.isDead() || !worldName.equals(livingEntity.getWorld().getName()) ? null : livingEntity;
    }

    @Nullable
    public Block getBlock() {
        if (blockLocation == null) return null;
        Block block = blockLocation.getBlock();
        return CompatibilityAPI.getBlockCompatibility().getHitBox(block) == null ? null : block;
    }
}