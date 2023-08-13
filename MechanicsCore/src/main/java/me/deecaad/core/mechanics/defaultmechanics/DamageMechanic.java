package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

public class DamageMechanic extends Mechanic {

    private double damage;

    private boolean requiresEvent;
    private boolean ignoreArmor

    /**
     * Default constructor for serializer
     */
    public DamageMechanic() {
    }

    public DamageMechanic(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public String getKeyword() {
        return "Ignite";
    }

    @Override
    protected void use0(CastData cast) {

        // We must have an entity to ignite
        if (cast.getTarget() == null)
            return;

        LivingEntity target = cast.getTarget();
        target.setMetadata("mechanicscore-damagemechanic", new FixedMetadataValue(MechanicsCore.getPlugin(), this));
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        int ticks = data.of("Time").getInt(100);

        return applyParentArgs(data, new IgniteMechanic(ticks));
    }
}
