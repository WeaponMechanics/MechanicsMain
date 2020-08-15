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
 * Targets the skill caster's mount, or null
 */
@SerializerData(name = "@mount")
public class MountTargeter implements Targeter<Entity> {

    /**
     * Default constructor for serializer
     */
    public MountTargeter() {
    }

    @Nullable
    @Override
    public List<Entity> getTargets(MechanicCaster caster) {
        if (caster instanceof EntityCaster) {
            EntityCaster entityCaster = (EntityCaster) caster;
            return Collections.singletonList(entityCaster.getEntity().getVehicle());
        } else {
            debug.warn("Tried to use @mount with a non entity caster");
        }

        return null;
    }

    @Override
    public Targeter<Entity> serialize(Map<String, Object> data) {
        // Do nothing
        return this;
    }
}
