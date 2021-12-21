package me.deecaad.weaponmechanics.weapon.newprojectile;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class WeaponProjectile extends AProjectile {

    private static final double STEP = 0.25;

    protected WeaponProjectile(ProjectileSettings projectileSettings, LivingEntity shooter, Location location, Vector motion) {
        super(projectileSettings, shooter, location, motion);
    }

    @Override
    public boolean handleCollisions() {



        return false;
    }

    public List<RayTraceResult> rayTrace(List<Block> possibleBlocks, List<Entity> possibleEntities) {



        return null;
    }

    public List<Block> getPossibleBlocks(Predicate<Block> filter) {

        List<Block> blocks = new ArrayList<>();

        for (double i = 0; i <= motionLength; i += STEP) {


        }

        return null;
    }



    private List<LivingEntity> getPossibleEntities() {

        // Get the box of current location to end of this iteration
        HitBox hitBox = new HitBox(location, location.clone().add(new Vector(motionLength, motionLength, motionLength)));

        int minX = floor((hitBox.getMinX() - 2.0D) / 16.0D);
        int maxX = floor((hitBox.getMaxX() + 2.0D) / 16.0D);
        int minZ = floor((hitBox.getMinZ() - 2.0D) / 16.0D);
        int maxZ = floor((hitBox.getMaxZ() + 2.0D) / 16.0D);

        List<LivingEntity> entities = new ArrayList<>();

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                Chunk chunk = world.getChunkAt(x, z);
                for (final Entity entity : chunk.getEntities()) {
                    if (!entity.getType().isAlive() || entity.isInvulnerable()) continue;

                    entities.add((LivingEntity) entity);
                }
            }
        }

        return entities.isEmpty() ? null : entities;
    }

    private int floor(double toFloor) {
        int flooredValue = (int) toFloor;
        return toFloor < flooredValue ? flooredValue - 1 : flooredValue;
    }
}