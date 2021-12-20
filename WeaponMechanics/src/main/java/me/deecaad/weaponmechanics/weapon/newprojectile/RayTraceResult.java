package me.deecaad.weaponmechanics.weapon.newprojectile;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class RayTraceResult {

    private Vector hitLocation;
    private HitBox hitBox;
    private BlockFace hitFace;
    private long hitTime;

    // If block
    private Block block;

    // If living entity
    private LivingEntity livingEntity;
    
}