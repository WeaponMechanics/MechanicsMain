package me.deecaad.weaponmechanics.weapon.explode.shapes;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * This explosion uses the minecraft explosion system,
 * instead of using a shape.
 *
 * https://minecraft.gamepedia.com/Explosion
 */
public class DefaultExplosion implements ExplosionShape {

    private static final double DECAY_RATE = 0.3;
    private static final double ABSORB_RATE = 0.3;

    private final float yield;
    private final int gridSize;
    private final int bound;
    
    public DefaultExplosion(double yield) {
        this(yield, 16);
    }

    public DefaultExplosion(double yield, int gridSize) {
        this.yield = (float) yield;
        this.gridSize = gridSize;
        this.bound = gridSize - 1;
    }

    @Nonnull
    @Override
    public List<Block> getBlocks(@Nonnull Location origin) {

        // If the explosion is too small, then no blocks are destroyed
        if (yield < 0.1F) {
            return new ArrayList<>(0);
        }

        World world = origin.getWorld();

        Set<Block> set = new HashSet<>();

        // Separates the explosion into a 16 by 16 by 16
        // grid.
        for (int k = 0; k < gridSize; ++k) {
            for (int i = 0; i < gridSize; ++i) {
                for (int j = 0; j < gridSize; ++j) {

                    // Checking if the the point defined by (k, i, j) is on the grid
                    if (k == 0 || k == bound || i == 0 || i == bound || j == 0 || j == bound) {

                        Vector vector = new Vector(((double) k) / bound * 2 - 1, ((double) i) / bound * 2 - 1, ((double) j) / bound * 2 - 1);
                        vector.normalize();

                        double x = origin.getX();
                        double y = origin.getY();
                        double z = origin.getZ();

                        // Slightly randomized intensity, based on the yield of the explosion
                        float intensity = yield * (0.7F + ThreadLocalRandom.current().nextFloat() * 0.6F);

                        while (intensity > 0.0f) {
                            Block block = world.getBlockAt((int) x, (int) y, (int) z);

                            Material type = block.getType();

                            if (!block.isEmpty()) {
                                float resistance = MaterialUtil.getBlastResistance(type);

                                intensity -= (resistance + 0.3F) * ABSORB_RATE;
                            }

                            if (intensity > 0.0F && y < 256 && y >= 0) {
                                set.add(block);
                            }

                            x += vector.getX() * DECAY_RATE;
                            y += vector.getY() * DECAY_RATE;
                            z += vector.getZ() * DECAY_RATE;

                            // Ray decays over longer distance
                            intensity -= DECAY_RATE * 0.75;
                        }
                    }
                }
            }
        }
        return new ArrayList<>(set);
    }

    @Override
    public List<LivingEntity> getEntities(@Nonnull Location origin) {

        // How far away from the explosion to damage players
        double damageRadius = yield * 2.0F;
        double damageRadiusOuter = damageRadius + 1;

        World world = origin.getWorld();

        if (world == null) {
            debug.log(LogLevel.ERROR, "Explosion in null world? Location: " + origin, "Please report error to devs");
            return null;
        }

        ArrayList<LivingEntity> entities = new ArrayList<>();
        for (Entity entity : world.getNearbyEntities(origin, damageRadiusOuter, damageRadiusOuter, damageRadiusOuter)) {
            if (entity.getType().isAlive()) {
                entities.add((LivingEntity) entity);
            }
        }

        return entities;
    }

    @Override
    public double getMaxDistance() {
        return yield * 2f;
    }

    @Override
    public String toString() {
        return "DefaultExplosion{" +
                "yield=" + yield +
                '}';
    }
}
