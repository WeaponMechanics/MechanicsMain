package me.deecaad.weaponmechanics.weapon;

import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public final class BlockDamageData implements Listener {

    private static final Map<Chunk, Map<Block, DamageData>> BLOCK_DAMAGE_MAP = new HashMap<>(1000);
    private static final int MAX_BLOCK_CRACK = 10;
    private static final Material AIR = Material.valueOf("AIR"); // Maybe we should have XMaterial

    public static void damageBlock(Block block, int amount, int maxDurability, boolean isBreak, int regenTime) {

        // Make sure to add data, if needed
        Map<Block, DamageData> chunkData = BLOCK_DAMAGE_MAP.putIfAbsent(block.getChunk(), new HashMap<>());
        DamageData blockData = chunkData.putIfAbsent(block, new DamageData(block));

        blockData.damage(amount, maxDurability);
        if (isBreak && blockData.isDestroyed()) {
            blockData.destroy();

            if (regenTime >= 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        blockData.regenerate();
                    }
                }.runTaskLater(WeaponMechanics.getPlugin(), regenTime);
            }
        }
    }

    public static boolean isBroken(Block block) {

        // Make sure to add data, if needed
        Map<Block, DamageData> chunkData = BLOCK_DAMAGE_MAP.putIfAbsent(block.getChunk(), new HashMap<>());
        DamageData blockData = chunkData.putIfAbsent(block, new DamageData(block));

        return blockData.isDestroyed();
    }

    public static void regenerate(Chunk chunk) {
        Map<Block, DamageData> chunkData = BLOCK_DAMAGE_MAP.get(chunk);

        if (chunkData == null) return;

        for (DamageData data : chunkData.values()) {

            if (data.isDestroyed()) data.regenerate();
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {

        Chunk chunk = e.getChunk();
        regenerate(chunk);
        BLOCK_DAMAGE_MAP.remove(chunk);
    }

    // todo on WorldSaveEvent


    private static class DamageData {

        private Block block;
        private Material regenType;
        private double durability;

        public DamageData(Block block) {
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
            BLOCK_DAMAGE_MAP.get(block.getChunk()).remove(block);
        }
    }
}
