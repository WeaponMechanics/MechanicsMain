package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightLevelCondition extends Condition {

    private LightLevelMode mode;
    private int min;
    private int max;

    /**
     * Default constructor for serializer.
     */
    public LightLevelCondition() {
    }

    public LightLevelCondition(LightLevelMode mode, int min, int max) {
        this.mode = mode;
        this.min = min;
        this.max = max;
    }

    public LightLevelMode getMode() {
        return mode;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public boolean isAllowed0(CastData cast) {
        Block block = cast.getTargetLocation().getBlock();
        int light = mode.getLightLevel(block);
        return min <= light && max >= light;
    }

    @Override
    public String getKeyword() {
        return "LightLevel";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/LightLevelCondition";
    }

    @NotNull
    @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        LightLevelMode mode = data.of("Mode").getEnum(LightLevelMode.class, LightLevelMode.BOTH);
        int min = data.of("Min").getInt(0);
        int max = data.of("Max").getInt(15);

        return applyParentArgs(data, new LightLevelCondition(mode, min, max));
    }

    public enum LightLevelMode {

        SKY {
            @Override
            int getLightLevel(Block block) {
                return block.getLightFromSky();
            }
        },
        BLOCK {
            @Override
            int getLightLevel(Block block) {
                return block.getLightFromBlocks();
            }
        },
        BOTH {
            @Override
            int getLightLevel(Block block) {
                return block.getLightLevel();
            }
        };

        abstract int getLightLevel(Block block);
    }
}
