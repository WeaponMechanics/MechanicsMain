package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.weaponmechanics.compatibility.projectile.HitBox;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * Class to hold collision data information
 */
public class CollisionData {

    private Vector hitLocation;
    private HitBox hitBox;
    private Block block;
    private LivingEntity livingEntity;

    public CollisionData(HitBox hitBox, Vector hitLocation, LivingEntity livingEntity) {
        this.hitBox = hitBox;
        this.hitLocation = hitLocation;
        this.livingEntity = livingEntity;
    }

    public CollisionData(HitBox hitBox, Vector hitLocation, Block block) {
        this.hitBox = hitBox;
        this.hitLocation = hitLocation;
        this.block = block;
    }

    /**
     * @return the hit block or entity's hit box
     */
    public HitBox getHitBox() {
        return hitBox;
    }

    public Vector getHitLocation() {
        return hitLocation;
    }

    public Block getBlock() {
        return block;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollisionData that = (CollisionData) o;
        return Objects.equals(block, that.block) &&
                Objects.equals(livingEntity, that.livingEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, livingEntity);
    }
}