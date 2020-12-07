package me.deecaad.weaponmechanics.weapon.explode.shapes;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialHelper;
import me.deecaad.core.utils.NumberUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * This explosion uses the minecraft explosion system,
 * instead of using a shape.
 *
 * https://minecraft.gamepedia.com/Explosion
 */
public class DefaultExplosion implements ExplosionShape {

    private static final int GRID_SIZE = 16; // 16 minecraft default
    private static final int BOUND = GRID_SIZE - 1;
    private static final double DECAY_RATE = 0.3;
    private static final double ABSORB_RATE = 0.3;

    private float yield;
    
    public DefaultExplosion(double yield) {
        this.yield = (float) yield;
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
        for (int k = 0; k < GRID_SIZE; ++k) {
            for (int i = 0; i < GRID_SIZE; ++i) {
                for (int j = 0; j < GRID_SIZE; ++j) {

                    // Checking if the the point defined by (k, i, j) is on the grid
                    if (k == 0 || k == BOUND || i == 0 || i == BOUND || j == 0 || j == BOUND) {

                        Vector vector = new Vector(((double) k) / BOUND * 2 - 1, ((double) i) / BOUND * 2 - 1, ((double) j) / BOUND * 2 - 1);
                        vector.normalize();

                        double x = origin.getX();
                        double y = origin.getY();
                        double z = origin.getZ();

                        // Slightly randomized intensity, based on the yield of the explosion
                        float intensity = yield * (0.7F + NumberUtils.random().nextFloat() * 0.6F);

                        while (intensity > 0.0f) {
                            Block block = world.getBlockAt((int) x, (int) y, (int) z);

                            Material type = block.getType();

                            if (!block.isEmpty()) {
                                float resistance = MaterialHelper.getBlastResistance(type);

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

        // Get all entities within the damageable radius
        // Only can damage LivingEntities
        return world.getNearbyEntities(origin, damageRadiusOuter, damageRadiusOuter, damageRadiusOuter, LivingEntity.class::isInstance)
                .stream()
                .map(LivingEntity.class::cast)
                .collect(Collectors.toList());
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
