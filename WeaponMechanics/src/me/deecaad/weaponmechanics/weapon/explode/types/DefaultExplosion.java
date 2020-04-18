package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * This explosion uses the minecraft explosion system,
 * instead of using a shape.
 */
public class DefaultExplosion implements Explosion {
    
    private float yield;
    
    public DefaultExplosion(double yield) {
        this.yield = (float) yield;
    }
    
    @Nonnull
    @Override
    public Set<Block> getBlocks(@Nonnull Location origin) {
        if (yield >= 0.1F) {
            World world = origin.getWorld();
            double xPos = origin.getX();
            double yPos = origin.getY();
            double zPos = origin.getZ();

            Set<Block> set = new HashSet<>();
            boolean flag = true;

            for (int k = 0; k < 16; ++k) {
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                            double d0 = ((float) k / 15.0F * 2.0F - 1.0F);
                            double d1 = ((float) i / 15.0F * 2.0F - 1.0F);
                            double d2 = ((float) j / 15.0F * 2.0F - 1.0F);
                            double length = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                            // Normalize
                            d0 /= length;
                            d1 /= length;
                            d2 /= length;

                            // Make copies of the origin coords
                            double x = xPos;
                            double y = yPos;
                            double z = zPos;

                            float f = yield * (0.7F + NumberUtils.random().nextFloat() * 0.6F);

                            for (float var21 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                Block block = world.getBlockAt((int)x, (int)y, (int)z);
                                Material type = block.getType();
                                BlockData data = block.getBlockData();

                                String name = type.name();
                                boolean isFluid = name.equals("WATER")
                                        || name.equals("LAVA")
                                        || name.equals("STATIONARY_WATER")
                                        || name.equals("STATIONARY_LAVA");

                                if (!type.isAir()|| isFluid) { // Maybe !isFluid
                                    float f2 = Math.max(type.getBlastResistance(), (isFluid ? 100.0f : 0.0f)); // only works 1.13+
                                    //if (this.source != null) {
                                    //    f2 = this.source.a(this, this.world, blockposition, iblockdata, fluid, f2);
                                    //}

                                    f -= (f2 + 0.3F) * 0.3F;
                                }

                                if (f > 0.0F /*&& (this.source == null || this.source.a(this, this.world, blockposition, iblockdata, f))*/ && block.getY() < 256 && block.getY() >= 0) {
                                    set.add(block);
                                }

                                x += d0 * 0.3;
                                y += d1 * 0.3;
                                z += d2 * 0.3;
                            }
                        }
                    }
                }
            }
            return set;
        }
        return new HashSet<>();
    }
    
    @Nonnull
    @Override
    public Set<LivingEntity> getEntities(@Nonnull Location origin) {
        // todo use default minecraft explosions
        return null;
    }
}
