package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.MapConfigLike;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.simple.RegistryValueSerializer;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OnGroundCondition extends Condition {

    private Set<BlockType> blocks;

    /**
     * Default constructor for serializer.
     */
    public OnGroundCondition() {
    }

    public OnGroundCondition(Set<BlockType> blocks) {
        this.blocks = blocks;
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        if (cast.getTarget() == null)
            return false;

        BlockType block = cast.getTargetLocation().getBlock().getRelative(BlockFace.DOWN).getType().asBlockType();
        return cast.getTarget().isOnGround() && blocks.contains(block);
    }

    @Override
    public String getKeyword() {
        return "On_Ground";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/conditions/on-ground";
    }

    @Override
    public @NotNull Condition serialize(@NotNull SerializeData data) throws SerializerException {
        List<MapConfigLike.Holder> materials = data.of("Blocks").get(List.class).get();
        Set<BlockType> blocks = new HashSet<>();

        for (MapConfigLike.Holder holder : materials) {
            String block = holder.value().toString();
            RegistryValueSerializer<BlockType> serializer = new RegistryValueSerializer<>(BlockType.class, true);
            List<BlockType> localBlocks = serializer.deserialize(block, data.of().getLocation());

            blocks.addAll(localBlocks);
        }

        return applyParentArgs(data, new OnGroundCondition(blocks));
    }
}