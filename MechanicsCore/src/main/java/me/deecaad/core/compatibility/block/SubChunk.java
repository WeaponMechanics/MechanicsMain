package me.deecaad.core.compatibility.block;

import org.bukkit.block.Block;

/**
 * In 1_16_R2 and higher, PacketPlayOutMultiBlockMask uses "subchunks" (16 x 16 x 16 area)
 * instead of chunks (16 x 256 x 16 area). This class defines that 16 x 16 x 16 area. Note
 * that the coordinates are chunk coordinates, not block coordinates.
 */
class SubChunk {

    private final int x;
    private final int y;
    private final int z;

    SubChunk(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    int getZ() {
        return z;
    }

    static SubChunk byBlock(Block block) {
        int x = block.getX() << 4;
        int y = block.getY() << 4;
        int z = block.getZ() << 4;

        return new SubChunk(x, y, z);
    }
}
