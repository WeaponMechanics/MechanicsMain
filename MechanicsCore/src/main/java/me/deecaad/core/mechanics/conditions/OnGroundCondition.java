package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.MapConfigLike;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerEnumException;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.utils.EnumUtil;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OnGroundCondition extends Condition {

    private Set<Material> blocks;

    /**
     * Default constructor for serializer.
     */
    public OnGroundCondition() {
    }

    public OnGroundCondition(Set<Material> blocks) {
        this.blocks = blocks;
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        if (cast.getTarget() == null)
            return false;

        Material material = cast.getTargetLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        return cast.getTarget().isOnGround() && blocks.contains(material);
    }

    @Override
    public String getKeyword() {
        return "On_Ground";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/OnGroundCondition";
    }

    @NotNull
    @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        List<MapConfigLike.Holder> materials = data.of("Blocks").assertType(List.class).get(List.of());
        Set<Material> blocks = new HashSet<>();

        for (MapConfigLike.Holder holder : materials) {
            String block = holder.value().toString();
            List<Material> temp = EnumUtil.parseEnums(Material.class, block);

            if (temp.isEmpty())
                throw new SerializerEnumException(this, Material.class, block, true, data.of("Blocks").getLocation());

            blocks.addAll(temp);
        }

        return applyParentArgs(data, new OnGroundCondition(blocks));
    }
}