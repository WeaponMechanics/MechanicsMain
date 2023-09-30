package me.deecaad.core.utils.ray;

import me.deecaad.core.compatibility.HitBox;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class BlockTraceResult extends RayTraceResult {

    private final @NotNull Block block;
    private final @NotNull BlockState blockState;

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
        this.blockState = block.getState();
    }

    /**
     * Returns the block that was hit. This block should only be used for x-y-z
     * coordinates. To get information about the block (material, data, etc.),
     * use {@link #getBlockState()}.
     *
     * @return The hit block.
     */
    @NotNull
    public Block getBlock() {
        return block;
    }

    /**
     * Returns information about the block that was hit.
     *
     * @return The hit block state.
     */
    @NotNull
    public BlockState getBlockState() {
        return blockState;
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
