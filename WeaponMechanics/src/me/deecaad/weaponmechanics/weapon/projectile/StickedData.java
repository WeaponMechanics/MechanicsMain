package me.deecaad.weaponmechanics.weapon.projectile;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class StickedData {

    private Location blockLocation;
    private LivingEntity livingEntity;
    private Vector relativeSpawnLocation;

    public StickedData(Location blockLocation, Vector hitLocation) {
        this.blockLocation = blockLocation;
        this.relativeSpawnLocation = blockLocation.subtract(hitLocation).toVector();
    }

    public StickedData(LivingEntity livingEntity, Vector hitLocation) {
        this.livingEntity = livingEntity;
        this.relativeSpawnLocation = livingEntity.getLocation().subtract(hitLocation).toVector();
    }

    public Vector getNewLocation() {
        if (livingEntity != null) {
            return livingEntity.isDead() ? null : livingEntity.getLocation().add(relativeSpawnLocation).toVector();
        }
        return blockLocation.getBlock().isEmpty() ? null : blockLocation.add(relativeSpawnLocation).toVector();
    }
}