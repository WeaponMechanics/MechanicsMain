package me.deecaad.weaponmechanics.weapon.newprojectile.weaponprojectile;

import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class RayTraceResult {

    private Vector hitLocation;
    private double distanceTravelled;
    private BlockFace hitFace;

    // If block
    private Block block;

    // If living entity
    private LivingEntity livingEntity;
    private DamagePoint hitPoint;

    public RayTraceResult(Vector hitLocation, double distanceTravelled, BlockFace hitFace, Block block) {
        this.hitLocation = hitLocation;
        this.distanceTravelled = distanceTravelled;
        this.hitFace = hitFace;
        this.block = block;
    }

    public RayTraceResult(Vector hitLocation, double distanceTravelled, BlockFace hitFace, LivingEntity livingEntity, DamagePoint hitPoint) {
        this.hitLocation = hitLocation;
        this.distanceTravelled = distanceTravelled;
        this.hitFace = hitFace;
        this.livingEntity = livingEntity;
        this.hitPoint = hitPoint;
    }

    public Vector getHitLocation() {
        return hitLocation;
    }

    public double getDistanceTravelled() {
        return distanceTravelled;
    }

    public BlockFace getHitFace() {
        return hitFace;
    }

    public Block getBlock() {
        return block;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public DamagePoint getHitPoint() {
        return hitPoint;
    }

    public boolean handleHit(WeaponProjectile weaponProjectile) {
        return this.block != null ? handleBlockHit(weaponProjectile) : handleEntityHit(weaponProjectile);
    }

    private boolean handleBlockHit(WeaponProjectile weaponProjectile) {

        // todo

        return false;
    }

    private boolean handleEntityHit(WeaponProjectile weaponProjectile) {

        // todo

        return false;
    }
}