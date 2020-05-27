package me.deecaad.weaponmechanics.weapon.explode.regeneration;

import me.deecaad.core.utils.NumberUtils;
import org.bukkit.block.Block;

import java.util.Comparator;

public class BlockSorter implements Comparator<Block> {

    private static Comparator<Block> comparingInt = Comparator.comparingInt(Block::getY);

    @Override
    public int compare(Block o1, Block o2) {
        int diff = comparingInt.compare(o1, o2);

        // Blocks are on the same Y level, add randomization
        if (diff == 0) {
            return NumberUtils.chance(0.50) ? -1 : 1;
        } else {
            return diff;
        }
    }
}
