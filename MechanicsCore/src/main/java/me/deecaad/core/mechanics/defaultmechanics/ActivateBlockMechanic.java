package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.PriorityQueue;
import java.util.function.Consumer;

public abstract class ActivateBlockMechanic<T extends BlockState> extends Mechanic {

    private final Class<T> blockClass;
    private int searchRadius;
    private int maxBlocks;

    /**
     * Default constructor for serializer.
     */
    public ActivateBlockMechanic(@NotNull Class<T> blockClass) {
        this.blockClass = blockClass;
    }

    public Class<T> getBlockClass() {
        return blockClass;
    }

    public double getSearchRadius() {
        return searchRadius;
    }

    public int getMaxBlocks() {
        return maxBlocks;
    }

    public void forEachBlock(@NotNull Location origin, @NotNull Consumer<T> blockFunction) {
        PriorityQueue<TileEntityDistance<T>> closestEntities = new PriorityQueue<>();
        Chunk originChunk = origin.getChunk();

        for (int dx = -searchRadius; dx <= searchRadius; dx += 16) {
            for (int dz = -searchRadius; dz <= searchRadius; dz += 16) {
                int chunkX = originChunk.getX() + dx >> 4;
                int chunkZ = originChunk.getZ() + dz >> 4;

                Chunk chunk = origin.getWorld().getChunkAt(chunkX, chunkZ);
                for (BlockState state : chunk.getTileEntities()) {
                    if (!blockClass.isInstance(state))
                        continue;

                    double dxState = state.getX() - origin.getX();
                    double dyState = state.getY() - origin.getY();
                    double dzState = state.getZ() - origin.getZ();

                    if (dxState >= -searchRadius && dxState <= searchRadius &&
                            dyState >= -searchRadius && dyState <= searchRadius &&
                            dzState >= -searchRadius && dzState <= searchRadius) {

                        double squaredDistance = dxState * dxState + dyState * dyState + dzState * dzState;
                        closestEntities.add(new TileEntityDistance<>(blockClass.cast(state), squaredDistance));
                    }
                }
            }
        }

        // Get the closest maxBlocks entities
        for (int i = 0; i < maxBlocks && !closestEntities.isEmpty(); i++) {
            T tileEntity = closestEntities.poll().tileEntity();
            blockFunction.accept(tileEntity);
        }
    }

    @Override
    public Mechanic applyParentArgs(SerializeData data, Mechanic mechanic) throws SerializerException {
        ActivateBlockMechanic<?> blockMechanic = (ActivateBlockMechanic<?>) super.applyParentArgs(data, mechanic);
        blockMechanic.maxBlocks = data.of("Max_Blocks").assertPositive().getInt(1);
        blockMechanic.searchRadius = (int) Math.ceil(data.of("Search_Radius").assertPositive().getDouble(8.0));
        return blockMechanic;
    }

    private record TileEntityDistance<T>(
            T tileEntity,
            double squaredDistance
    ) implements Comparable<TileEntityDistance<T>> {
        @Override
        public int compareTo(TileEntityDistance<T> other) {
            return Double.compare(this.squaredDistance, other.squaredDistance);
        }
    }
}
