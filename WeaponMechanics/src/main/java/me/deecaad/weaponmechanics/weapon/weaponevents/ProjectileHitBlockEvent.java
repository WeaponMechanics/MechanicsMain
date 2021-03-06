package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * This class outlines the event of a projectile hitting a block. If this event
 * is cancelled, the projectile will not "interact" with the block. This can
 * cancel explosions, block damage, etc.
 */
public class ProjectileHitBlockEvent extends ProjectileEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Block block;
    private final BlockFace hitBlockFace;
    private final Vector exactLocation;
    private boolean isCancelled;

    public ProjectileHitBlockEvent(WeaponProjectile projectile, Block block, BlockFace hitBlockFace, Vector exactLocation) {
        super(projectile);
        this.block = block;
        this.hitBlockFace = hitBlockFace;
        this.exactLocation = exactLocation;
    }

    /**
     * Returns the block that was hit by the projectile.
     *
     * @return The non-null bukkit block that was hit.
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Returns the face of the block that was hit by the projectile.
     *
     * @return The non-null hit block face.
     */
    public BlockFace getHitFace() {
        return hitBlockFace;
    }

    /**
     * Returns the <i>exact*</i> location where the projectile's ray
     * collides with the block's bounding box.
     *
     * @return The non-null collision point
     */
    public Location getHitLocation() {
        return exactLocation.toLocation(projectile.getWorld());
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
