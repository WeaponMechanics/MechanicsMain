package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class StickedData {

    private Location blockLocation;
    private LivingEntity livingEntity;
    private final Vector relativeSpawnLocation;
    private final String worldName;

    public StickedData(Location blockLocation, Vector hitLocation) {
        this.blockLocation = blockLocation;
        this.relativeSpawnLocation = hitLocation.subtract(blockLocation.clone().toVector());
        this.worldName = blockLocation.getWorld().getName();
    }

    public StickedData(LivingEntity livingEntity, Vector hitLocation) {
        this.livingEntity = livingEntity;
        this.relativeSpawnLocation = hitLocation.subtract(livingEntity.getLocation().toVector());
        this.worldName = livingEntity.getWorld().getName();
    }

    @Nullable
    public Vector getNewLocation() {
        if (livingEntity != null) {
            return livingEntity.isDead() || !worldName.equals(livingEntity.getWorld().getName()) ? null : livingEntity.getLocation().clone().add(relativeSpawnLocation).toVector();
        }
        return WeaponCompatibilityAPI.getProjectileCompatibility().getHitBox(blockLocation.getBlock()) == null ? null : blockLocation.clone().add(relativeSpawnLocation).toVector();
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