package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

public class BiomeCondition extends Condition {

    private Biome biome;

    /**
     * Default constructor for serializer.
     */
    public BiomeCondition() {
    }

    public BiomeCondition(Biome biome) {
        this.biome = biome;
    }

    @Override
    public boolean isAllowed0(CastData cast) {
        return cast.getTargetLocation().getBlock().getBiome() == biome;
    }

    @Override
    public String getKeyword() {
        return "Biome";
    }

    @NotNull
    @Override
    public Condition serialize(SerializeData data) throws SerializerException {
        Biome biome = data.of("Biome").assertExists().getEnum(Biome.class);
        return applyParentArgs(data, new BiomeCondition(biome));
    }
}
