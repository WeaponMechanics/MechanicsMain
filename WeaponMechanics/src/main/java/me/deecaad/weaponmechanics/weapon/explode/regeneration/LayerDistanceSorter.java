package me.deecaad.weaponmechanics.weapon.explode.regeneration;

import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class LayerDistanceSorter extends BlockRegenSorter {

    public LayerDistanceSorter(Location origin, Explosion explosion) {
        super(origin, explosion);
    }

    @Override
    public int compare(Block o1, Block o2) {
        int height = o1.getY() - o2.getY();

        if (height != 0) return height;
        else return (int) (origin.distanceSquared(o2.getLocation()) - origin.distanceSquared(o1.getLocation()));
    }
}
