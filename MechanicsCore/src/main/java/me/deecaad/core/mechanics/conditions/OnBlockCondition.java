package me.vierdant.bridgedmechanics.tmep;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.conditions.Condition;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class OnBlockCondition extends Condition {

    private Set<Material> blocks = new HashSet<>();

    public OnBlockCondition() {}

    public OnBlockCondition(Set<Material> blocks) {
        this.blocks = blocks;
    }

    @Override
    protected boolean isAllowed0(CastData cast) {
        if (cast.getTarget() == null) return false;
        Material material = cast.getTargetLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        return this.blocks.contains(material);
    }

    @Override
    public String getKeyword() {
        return "OnBlock";
    }

    @NotNull
    @Override
    public Condition serialize(SerializeData data) throws SerializerException {
        String materials = data.of("Blocks").assertExists().get();
        String[] splitMaterials = materials.split(",");
        Set<Material> blocks = new HashSet<>();

        for (String block : splitMaterials) {
            try {
                Material materialValue = Material.valueOf(block.toUpperCase());
                blocks.add(materialValue);
            } catch (Exception ex) {
                throw data.exception("'" + block + "' is not a valid Material.");
            }
        }

        return applyParentArgs(data, new OnBlockCondition(blocks));
    }
}