package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.utils.NumberUtil;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class BlockDamageData implements Listener {

    private static final Map<Chunk, Map<Block, DamageData>> BLOCK_DAMAGE_MAP = new HashMap<>(1000);
    private static final int MAX_BLOCK_CRACK = 9;
    private static final Material AIR = Material.valueOf("AIR");

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

        blockData.damage(amount, maxDurability, isBreak);
        if (blockData.isDestroyed()) {
            if (isBreak) blockData.destroy();

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

        // There are no blocks needed to regenerate
        if (chunkData == null) {
            return;
        } else {
            // Cloning to avoid concurrent modification
            chunkData = new HashMap<>(chunkData);
        }

        for (DamageData data : chunkData.values()) {

            if (data.isDestroyed()) {
                data.regenerate();
                data.remove();
            }
        }
    }

    public static void regenerateAll() {

        // Cloning to avoid concurrent modification
        Set<Chunk> chunks = new HashSet<>(getBlockDamageMap().keySet());
        for (Chunk chunk: chunks) {
            regenerate(chunk);
        }
    }

    private static class DamageData {

        private final Block block;
        private double durability;
        private BlockState state;
        private int id = -1;

        DamageData(Block block) {
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
        boolean isDestroyed() {

            // Trying to fix floating point math inaccuracies
            // by creating my own inaccuracy
            return durability < 0.0000001;
        }

        /**
         * Should damage this block for (damageAmount / maxDamage)
         * amount of damage
         *
         * @param damageAmount The amount of damage, should be lower then maxDamage
         * @param maxDamage The maximum amount of damage this weapon can deal to a block
         */
        public void damage(int damageAmount, int maxDamage, boolean isBreak) {
            this.durability -= ((double) damageAmount) / ((double) maxDamage);

            if (isBreak) {
                if (!isDestroyed())
                    update();
            } else {
                update();
            }
        }

        /**
         * Destroys this block
         */
        void destroy() {
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
         */
        void regenerate() {
            if (state != null) {
                // Update the state without applying physics
                // Updating the state will update byte data, material,
                // state, and data.
                state.update(true, false);

                // Reset the variables
                state = null;
            }
            durability = 1.0;

            // If the regen time is below 200 ticks (estimation),
            // then the block will still be cracked after the
            // reset, so we need to update it
            update();
        }

        /**
         * Updates the packet crack amount for all players
         * in this block's world
         */
        private void update() {

            if (id == -1) {
                id = BlockCompatibility.IDS.incrementAndGet();
            }

            int blockCrack;
            if (durability >= 1.0) {
                blockCrack = -1;
            } else {
                blockCrack = (int) ((1.0 - durability) * MAX_BLOCK_CRACK);
                blockCrack = NumberUtil.minMax(0, blockCrack, MAX_BLOCK_CRACK);
            }

            BlockCompatibility compatibility = CompatibilityAPI.getCompatibility().getBlockCompatibility();
            Object packet = compatibility.getCrackPacket(block, blockCrack, id);

            block.getWorld().getPlayers().forEach(player -> CompatibilityAPI.getCompatibility().sendPackets(player, packet));
        }

        /**
         * Removes this <code>DamageData</code>
         * from the main map
         */
        public void remove() {
            Map<Block, DamageData> data = BLOCK_DAMAGE_MAP.get(block.getChunk());
            if (data != null) data.remove(block);
        }
    }
}
