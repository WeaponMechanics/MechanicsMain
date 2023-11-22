package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.JarSearcherExempt;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.block.SculkCatalyst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SculkBloomMechanic extends ActivateBlockMechanic<SculkCatalyst> implements JarSearcherExempt {

    private int charge;

    /**
     * Default constructor for serializer
     */
    public SculkBloomMechanic() {
        super(SculkCatalyst.class);
    }

    public SculkBloomMechanic(int charge) {
        super(SculkCatalyst.class);
        this.charge = charge;
    }

    @Override
    public @Nullable String getKeyword() {
        return "SculkBloom";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/mechanics/sculk-bloom";
    }

    @Override
    protected void use0(CastData cast) {
        Location target = cast.getTargetLocation();
        if (target == null)
            return;

        forEachBlock(target, catalyst -> catalyst.bloom(target.getBlock(), charge));
    }

    @Override
    public @NotNull Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        // This should never be true since we only register this Mechanic in 1.20.2+
        if (ReflectionUtil.getMCVersion() < 20) {
            throw data.exception(null, "The SculkBloom{} Mechanic is only available in 1.20.2+");
        }

        int charge = data.of("Charge").assertRange(1, 32000).getInt(5);
        return applyParentArgs(data, new SculkBloomMechanic(charge));
    }
}
