package me.deecaad.weaponmechanics.listeners;

import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;

/**
 * Listens and cancels events that could interfere
 * with block regeneration, or cause damage because
 * of the missing blocks
 *
 * It also regenerates blocks if chunks unload/save
 */
public class ExplosionInteractionListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {

        Chunk chunk = e.getChunk();
        BlockDamageData.regenerate(chunk);
        BlockDamageData.getBlockDamageMap().remove(chunk);
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent e) {

        for (Chunk chunk : BlockDamageData.getBlockDamageMap().keySet()) {

            // Filter out chunks not from the world being saved
            if (!chunk.getWorld().equals(e.getWorld())) continue;

            BlockDamageData.regenerate(chunk);
        }
    }

    @EventHandler
    public void onItemFrameBreak(HangingBreakEvent e) {
        BlockFace attachment = e.getEntity().getAttachedFace();
        Block in = e.getEntity().getLocation().getBlock();

        Block attachedTo = in.getRelative(attachment);

        e.setCancelled(BlockDamageData.isBroken(attachedTo));
    }
}
