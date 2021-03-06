package me.deecaad.weaponmechanics.weapon.explode.raytrace;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class TraceResult {

    private final Set<Entity> entities;
    private final Set<Block> blocks;

    TraceResult(@Nullable Entity entity, @Nullable Block block) {
        entities = Collections.singleton(entity);
        blocks = Collections.singleton(block);
    }

    TraceResult(@Nonnull Set<Entity> entities, @Nonnull Set<Block> blocks) {
        this.entities = entities;
        this.blocks = blocks;
    }

    @Nonnull
    public Set<Entity> getEntities() {
        return entities;
    }

    /**
     * @return The first Entity found in the RayTrace
     */
    @Nullable
    public Entity getOneEntity() {
        return entities.stream().findFirst().orElse(null);
    }

    @Nonnull
    public Set<Block> getBlocks() {
        return blocks;
    }

    /**
     * @return The first Block found in the RayTrace
     */
    @Nullable
    public Block getOneBlock() {
        return blocks.stream().findFirst().orElse(null);
    }

    /**
     * Returns <code>true</code> if no blocks and no entities were hit during
     * the ray trace.
     *
     * @return true if nothing was hit
     */
    public boolean isEmpty() {
        return blocks.isEmpty() && entities.isEmpty();
    }
}
