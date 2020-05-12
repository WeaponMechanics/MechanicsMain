package me.deecaad.weaponmechanics.weapon;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class BlockDamageData {

    private static final Map<Block, BlockDamageData> BLOCK_DAMAGE_MAP = new HashMap<>(1000);
    private static final int MAX_BLOCK_CRACK = 10;
    private static final Material AIR = Material.valueOf("AIR"); // Maybe we should have XMaterial

    private Block block;
    private Material regenType;
    private double durability;

    public BlockDamageData(Block block) {
        this.block = block;
    }

    public Material getType() {
        return block.getType();
    }

    public double getDurability() {
        return durability;
    }

    public boolean isDestroyed() {

        // Trying to account for double math inaccuracies
        // by creating my own inaccuracy
        return durability > 0.99;
    }

    /**
     *
     * @param amount
     * @param maxDurability
     */
    public void damage(int amount, int maxDurability) {
        this.durability = ((double) amount) / ((double) maxDurability);

        // TODO display block crack based on this data's durability
        int blockCrack = (int) (durability * MAX_BLOCK_CRACK);
        if (blockCrack < MAX_BLOCK_CRACK) {
            // compatibility
        }
    }

    /**
     * Destroyed this block and saves
     * the previous type for later regeneration
     */
    public void destroy() {
        regenType = block.getType();
        block.setType(AIR, false);
    }

    /**
     * Regenerated the block contained by this data. If
     * the block has not yet been destroyed, then an
     * <code>IllegalStateException</code> is thrown
     *
     * @throws IllegalStateException Called before method call to destroy()
     */
    public void regenerate() {
        if (regenType == null) {
            throw new IllegalStateException("Attempted to regenerate block before it was destroyed");
        }

        block.setType(regenType, false);
        remove();
    }

    /**
     * Removes this data from the map of
     * data. Useful for saving memory
     * (and CPU if the map is really big)
     */
    public void remove() {
        BLOCK_DAMAGE_MAP.remove(block);
    }
}
