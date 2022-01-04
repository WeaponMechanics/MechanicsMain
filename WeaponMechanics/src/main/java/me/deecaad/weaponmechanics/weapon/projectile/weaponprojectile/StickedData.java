package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class StickedData {

    private Location blockLocation;
    private LivingEntity livingEntity;
    private final Vector relativeLocation;
    private final String worldName;

    public StickedData(RayTraceResult hit) {
        if (hit.isBlock()) {
            blockLocation = hit.getBlock().getLocation();
            relativeLocation = hit.getHitLocation().clone().subtract(blockLocation.toVector());
            worldName = blockLocation.getWorld().getName();
        } else {
            livingEntity = hit.getLivingEntity();
            relativeLocation = hit.getHitLocation().clone().subtract(livingEntity.getLocation().toVector());
            worldName = livingEntity.getWorld().getName();
        }
    }

    public Vector getNewLocation() {
        if (livingEntity != null) {
            return livingEntity.isDead() || !worldName.equals(livingEntity.getWorld().getName()) ? null : livingEntity.getLocation().clone().add(relativeLocation).toVector();
        }
        return WeaponCompatibilityAPI.getProjectileCompatibility().getHitBox(blockLocation.getBlock()) == null ? null : blockLocation.clone().add(relativeLocation).toVector();
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
        return WeaponCompatibilityAPI.getProjectileCompatibility().getHitBox(block) == null ? null : block;
    }
}