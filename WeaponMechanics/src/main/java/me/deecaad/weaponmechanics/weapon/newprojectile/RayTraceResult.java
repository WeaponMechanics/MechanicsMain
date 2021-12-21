package me.deecaad.weaponmechanics.weapon.newprojectile;

import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class RayTraceResult {

    private Vector hitLocation;
    private BlockFace hitFace;

    // If block
    private Block block;

    // If living entity
    private LivingEntity livingEntity;
    private DamagePoint hitPoint;

    public RayTraceResult(Vector hitLocation, BlockFace hitFace, Block block) {
        this.hitLocation = hitLocation;
        this.hitFace = hitFace;
        this.block = block;
    }

    public RayTraceResult(Vector hitLocation, BlockFace hitFace, LivingEntity livingEntity, DamagePoint hitPoint) {
        this.hitLocation = hitLocation;
        this.hitFace = hitFace;
        this.livingEntity = livingEntity;
        this.hitPoint = hitPoint;
    }
}