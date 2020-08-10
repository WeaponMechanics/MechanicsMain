package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.casters.EntityCaster;
import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

/**
 * Targets the owner of the skill caster.
 */
@SerializerData(name = "@owner")
public class OwnerTargeter implements Targeter<Entity> {

    /**
     * Default constructor for serializer
     */
    public OwnerTargeter() {
    }

    @Nullable
    @Override
    public List<Entity> getTargets(MechanicCaster caster) {
        if (caster instanceof EntityCaster) {
            EntityCaster entityCaster = (EntityCaster) caster;

            if (entityCaster.getEntity() instanceof Tameable) {
                Tameable tameable = (Tameable) entityCaster.getEntity();

                if (tameable.getOwner() instanceof Entity) {
                    return Collections.singletonList((Entity) tameable.getOwner());
                }
            }
        } else {
            debug.warn("Tried to use @owner on a non entity caster");
        }

        return null;
    }

    @Override
    public Targeter<Entity> serialize(Map<String, Object> data) {
        // Do nothing...
        return this;
    }
}
