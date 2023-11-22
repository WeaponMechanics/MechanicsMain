package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.JarSearcherExempt;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.block.SculkShrieker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SculkShriekMechanic extends ActivateBlockMechanic<SculkShrieker> implements JarSearcherExempt {

    public SculkShriekMechanic() {
        super(SculkShrieker.class);
    }

    @Override
    public @Nullable String getKeyword() {
        return "SculkShriek";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/mechanics/sculk-shriek";
    }

    @Override
    protected void use0(CastData cast) {
        LivingEntity target = cast.getTarget();
        if (!(target instanceof Player player))
            return;

        forEachBlock(player.getLocation(), shrieker -> shrieker.tryShriek(player));
    }

    @Override
    public @NotNull Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        // This should never be true since we only register this Mechanic in 1.20.2+
        if (ReflectionUtil.getMCVersion() < 20) {
            throw data.exception(null, "The SculkShriek{} Mechanic is only available in 1.20.2+");
        }

        return applyParentArgs(data, new SculkShriekMechanic());
    }
}
