package me.deecaad.weaponmechanics.weapon.explode.types;

import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * This explosion uses the minecraft explosion system,
 * instead of using a shape.
 */
public class DefaultExplosion implements Explosion {
    
    private double yield;
    
    public DefaultExplosion(double yield) {
        this.yield = yield;
    }
    
    @Nonnull
    @Override
    public Set<Block> getBlocks(@Nonnull Location origin) {
        // todo use default minecraft explosions
        return null;
    }
    
    @Nonnull
    @Override
    public Set<LivingEntity> getEntities(@Nonnull Location origin) {
        // todo use default minecraft explosions
        return null;
    }
}
