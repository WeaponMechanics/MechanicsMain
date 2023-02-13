package me.vierdant.bridgedmechanics.tmep;

import me.deecaad.core.file.MapConfigLike;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.conditions.Condition;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OnGroundCondition extends Condition {

    private Set<Material> blocks = new HashSet<>();

    public OnGroundCondition() {}

    public OnGroundCondition(Set<Material> blocks) {
        this.blocks = blocks;
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        if (cast.getTarget() == null) return false;

        if (blocks.isEmpty()) {
            return cast.getTarget().isOnGround();
        }

        Material material = cast.getTargetLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        return this.blocks.contains(material);
    }

    @Override
    public String getKeyword() {
        return "OnGround";
    }

    @NotNull
    @Override
    public Condition serialize(SerializeData data) throws SerializerException {
        List<MapConfigLike.Holder> materials = data.of("Blocks").assertType(List.class).get(List.of());
        Set<Material> blocks = new HashSet<>();

        for (MapConfigLike.Holder holder : materials) {
            String block = holder.value().toString();
            try {
                Material materialValue = Material.valueOf(block.toUpperCase());
                blocks.add(materialValue);
            } catch (Exception ex) {
                throw data.exception("'" + block + "' is not a valid Material.");
            }
        }

        return applyParentArgs(data, new OnGroundCondition(blocks));
    }
}