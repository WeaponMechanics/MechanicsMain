package me.deecaad.weaponmechanics.weapon;

import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public final class BlockDamageData implements Listener {

    private static final Map<Chunk, Map<Block, DamageData>> BLOCK_DAMAGE_MAP = new HashMap<>(1000);
    private static final int MAX_BLOCK_CRACK = 10;
    private static final Material AIR = Material.valueOf("AIR"); // Maybe we should have XMaterial

    /**
     * Don't let anyone instantiate this class
     */
    private BlockDamageData() {
    }

    public static Map<Chunk, Map<Block, DamageData>> getBlockDamageMap() {
        return BLOCK_DAMAGE_MAP;
    }

    public static void damageBlock(Block block, int amount, int maxDurability, boolean isBreak, int regenTime) {

        // Make sure to add data, if needed
        Map<Block, DamageData> chunkData = BLOCK_DAMAGE_MAP.putIfAbsent(block.getChunk(), new HashMap<>());
        DamageData blockData = chunkData.computeIfAbsent(block, DamageData::new);

        blockData.damage(amount, maxDurability);
        if (isBreak && blockData.isDestroyed()) {
            blockData.destroy();

            if (regenTime >= 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        blockData.regenerate();
                        blockData.remove();
                    }
                }.runTaskLater(WeaponMechanics.getPlugin(), regenTime);
            } else {
                blockData.remove();
            }
        }
    }

    public static boolean isBroken(Block block) {

        // Make sure to add data, if needed
        Map<Block, DamageData> chunkData = BLOCK_DAMAGE_MAP.computeIfAbsent(block.getChunk(), chunk -> new HashMap<>());
        DamageData blockData = chunkData.computeIfAbsent(block, DamageData::new);

        return blockData.isDestroyed();
    }

    public static void regenerate(Chunk chunk) {
        Map<Block, DamageData> chunkData = BLOCK_DAMAGE_MAP.get(chunk);

        if (chunkData == null) {
            return;
        }

        for (DamageData data : chunkData.values()) {

            if (data.isDestroyed()) data.regenerate();
        }
    }

    private static class DamageData {

        private final Block block;
        private double durability;

        // Variables used in regeneration
        private BlockState state;

        public DamageData(Block block) {
            this.block = block;
            durability = 1.0;
        }

        /**
         * Gets the durability of this block
         *
         * @return This block's durability
         */
        public double getDurability() {
            return durability;
        }

        /**
         * Tells you whether or not the <code>Block</code>
         * associated with this <code>DamageData</code> has
         * been destroyed
         *
         * @return true if the block is destroyed
         */
        public boolean isDestroyed() {

            // Trying to fix floating point math inaccuracies
            // by creating my own inaccuracy
            return durability < 0.0001;
        }

        /**
         * Should damage this block for (damageAmount / maxDamage)
         * amount of damage
         *
         * @param damageAmount The amount of damage, should be lower then maxDamage
         * @param maxDamage The maximum amount of damage this weapon can deal to a block
         */
        public void damage(int damageAmount, int maxDamage) {
            this.durability -= ((double) damageAmount) / ((double) maxDamage);

            // TODO display block crack based on this data's durability
            int blockCrack = (int) (durability * MAX_BLOCK_CRACK);
            if (blockCrack < MAX_BLOCK_CRACK) {
                // compatibility
            }
        }

        /**
         * Destroys this block
         */
        public void destroy() {
            state = block.getState();

            // Clear the contents of the inventory, if present
            // to avoid spawning dropped items
            if (state instanceof InventoryHolder) {
                if (state instanceof Chest) {
                    Inventory inv = ((Chest) state).getBlockInventory();
                    inv.clear();
                } else {
                    Inventory inv = ((InventoryHolder) state).getInventory();
                    inv.clear();
                }
            }

            // Set the type to AIR and do not apply physics
            block.setType(AIR, false);
        }

        /**
         * Regenerates this block
         *
         * @throws IllegalStateException If the block has not been destroyed
         */
        public void regenerate() {
            if (state == null) {
                throw new IllegalStateException("Call to regenerate() before destroy()");
            }

            // Update the state without applying physics
            // Updating the state will update byte data, material,
            // state, and data.
            state.update(true, false);

            // Reset the variables
            state = null;
            durability = 1.0;
        }

        /**
         * Removes this <code>DamageData</code>
         * from the main map
         */
        public void remove() {
            BLOCK_DAMAGE_MAP.get(block.getChunk()).remove(block);
        }
    }
}
