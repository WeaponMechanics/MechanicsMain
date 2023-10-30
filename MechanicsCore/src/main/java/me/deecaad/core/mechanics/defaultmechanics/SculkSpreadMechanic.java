package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Location;
import org.bukkit.block.SculkCatalyst;
import org.jetbrains.annotations.NotNull;

public class SculkSpreadMechanic extends ActivateBlockMechanic<SculkCatalyst> {

    private int charge;

    /**
     * Default constructor for serializer
     */
    public SculkSpreadMechanic() {
        super(SculkCatalyst.class);
    }

    public SculkSpreadMechanic(int charge) {
        super(SculkCatalyst.class);
        this.charge = charge;
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
        int charge = data.of("Charge").assertRange(1, 32000).getInt(5);
        return applyParentArgs(data, new SculkSpreadMechanic(charge));
    }
}
