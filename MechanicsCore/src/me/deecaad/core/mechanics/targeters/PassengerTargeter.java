package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.casters.EntityCaster;
import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

/**
 * Targets all of the caster's passengers
 */
@SerializerData(name = "@passenger")
public class PassengerTargeter implements Targeter<Entity> {

    /**
     * Default constructor for serializer
     */
    public PassengerTargeter() {
    }

    @Nullable
    @Override
    public List<Entity> getTargets(MechanicCaster caster) {
        if (caster instanceof EntityCaster) {
            return Collections.singletonList(((EntityCaster) caster).getEntity().getPassenger());
        } else {
            debug.warn("Tried to use @passenger on a non entity caster");
        }

        return null;
    }

    @Override
    public Targeter<Entity> serialize(Map<String, Object> data) {
        // Do nothing...
        return this;
    }
}
