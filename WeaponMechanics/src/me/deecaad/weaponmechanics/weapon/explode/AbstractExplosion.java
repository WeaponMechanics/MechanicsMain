package me.deecaad.weaponmechanics.weapon.explode;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public abstract class AbstractExplosion implements Explosion {

    public AbstractExplosion() {
    }

    public void explode(Location loc) {
        List<Block> blocks = getBlocks(loc);
    }
}
