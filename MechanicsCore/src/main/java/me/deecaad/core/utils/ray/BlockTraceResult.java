package me.deecaad.core.utils.ray;

import me.deecaad.core.compatibility.HitBox;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class BlockTraceResult extends RayTraceResult {

    private final @NotNull Block block;

    public BlockTraceResult(
            @NotNull Vector origin,
            @NotNull Vector direction,
            @NotNull HitBox hitBox,
            @NotNull BlockFace hitFace,
            @NotNull BlockFace exitFace,
            double hitMin,
            double hitMax,
            @NotNull Block block
    ) {
        super(origin, direction, hitBox, hitFace, exitFace, hitMin, hitMax);
        this.block = block;
    }

    /**
     * Returns the block that was hit.
     *
     * @return The block.
     */
    @NotNull
    public Block getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return "BlockTraceResult{" +
                "block=" + block +
                ", hitFace=" + super.getHitFace() +
                ", hitLocation=" + super.getHitLocation() +
                ", hitMin=" + super.getHitMin() +
                ", hitMax=" + super.getHitMax() +
                '}';
    }
}
