package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.util.Vector;

public class ProjectileHitBlockEvent extends ProjectileEvent implements Cancellable {

    private final Block block;
    private final BlockFace hitBlockFace;
    private final Vector exactLocation;
    private boolean isCancelled;

    public ProjectileHitBlockEvent(ICustomProjectile projectile, Block block, BlockFace hitBlockFace, Vector exactLocation) {
        super(projectile);
        this.block = block;
        this.hitBlockFace = hitBlockFace;
        this.exactLocation = exactLocation;
    }

    public Block getBlock() {
        return block;
    }

    public BlockFace getHitFace() {
        return hitBlockFace;
    }

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
}
