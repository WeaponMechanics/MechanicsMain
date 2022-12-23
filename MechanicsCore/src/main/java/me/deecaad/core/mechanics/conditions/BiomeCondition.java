package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.types.EnumType;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.block.Biome;

import java.util.Map;

public class BiomeCondition extends Condition {

    public static final Argument BIOME = new Argument("biome", new EnumType<>(Biome.class));

    private final Biome biome;

    public BiomeCondition(Map<Argument, Object> args) {
        biome = (Biome) args.get(BIOME);
    }

    @Override
    public ArgumentMap args() {
        return new ArgumentMap(BIOME);
    }

    @Override
    public String getKeyword() {
        return "Biome";
    }

    @Override
    public boolean isAllowed(CastData cast) {
        return cast.getTargetLocation().getBlock().getBiome() == biome;
    }
}
