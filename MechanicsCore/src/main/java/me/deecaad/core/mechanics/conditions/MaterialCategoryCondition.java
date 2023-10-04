package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialCategoryCondition extends Condition {

    private MaterialCategory category;

    /**
     * Default constructor for serializer.
     */
    public MaterialCategoryCondition() {
    }

    public MaterialCategoryCondition(MaterialCategory category) {
        this.category = category;
    }

    @Override
    public boolean isAllowed0(CastData cast) {
        return category.test(cast.getTargetLocation().getBlock());
    }

    @Override
    public String getKeyword() {
        return "Material_Category";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/MaterialCategoryCondition";
    }

    @NotNull
    @Override
    public Condition serialize(@NotNull SerializeData data) throws SerializerException {
        MaterialCategory category = data.of("Category").assertExists().getEnum(MaterialCategory.class);
        return applyParentArgs(data, new MaterialCategoryCondition(category));
    }

    /**
     * This is used to determine what "kind" of block you are in. Sometimes,
     * we want sounds to have an echo, but only when the player is in an
     * enclosed space. Instead of running calculations every time we play the
     * sound, we simply check if we are in cave_air (Vanilla generates caves
     * using cave_air). This is also good for adventure maps, who can replace
     * normal air with cave_air.
     */
    public enum MaterialCategory {

        ALL {
            @Override
            public boolean test(Block block) {
                return true;
            }
        },
        AIR {
            @Override
            public boolean test(Block block) {
                return !FLUID.test(block) && !CAVE_AIR.test(block) && !VOID_AIR.test(block);
            }
        },
        FLUID {
            @Override
            public boolean test(Block block) {
                if (ReflectionUtil.getMCVersion() < 13)
                    return block.isLiquid();

                if (block.isLiquid())
                    return true;
                else if (block.getBlockData() instanceof Waterlogged)
                    return ((Waterlogged) block.getBlockData()).isWaterlogged();
                else
                    return false;
            }
        },
        CAVE_AIR {
            @Override
            public boolean test(Block block) {
                return ReflectionUtil.getMCVersion() >= 13 && block.getType() == Material.CAVE_AIR;
            }
        },
        VOID_AIR {
            @Override
            public boolean test(Block block) {
                return ReflectionUtil.getMCVersion() >= 13 && block.getType() == Material.VOID_AIR;
            }
        };

        public abstract boolean test(Block block);

        public boolean test(Player player) {
            return test(player.getEyeLocation().getBlock());
        }
    }
}
