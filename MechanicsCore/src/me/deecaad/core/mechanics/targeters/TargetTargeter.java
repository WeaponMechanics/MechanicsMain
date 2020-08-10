package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.casters.EntityCaster;
import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

/**
 * If the <code>MechanicCaster</code> is a <code>Mob</code>, and the
 * <code>Mob</code> has a target, then this will target that target
 *
 * Otherwise, nothing is targeted
 */
@SerializerData(name = "@target")
public class TargetTargeter implements Targeter<LivingEntity> {

    /**
     * Default constructor for serializers
     */
    public TargetTargeter() {
    }

    @Override
    public List<LivingEntity> getTargets(MechanicCaster caster) {

        // Since this targeter is a bit weird, I am making sure to tell users if they are using it wrong
        if (caster instanceof EntityCaster) {
            EntityCaster entityCaster = (EntityCaster) caster;

            if (entityCaster.getEntity() instanceof Mob) {
                LivingEntity entity = ((Mob) entityCaster.getEntity()).getTarget();
                if (entity != null) return Collections.singletonList(entity);
                else debug.debug(entity.getName() + " can have a target, but doesn't currently have one");
            } else {
                debug.debug(entityCaster.getEntity().getType() + " can not have targets");
            }
        } else {
            debug.warn("Tried to use @target while not using an entity to cast the mechanic");
        }

        return null;
    }

    @Override
    public Targeter<LivingEntity> serialize(Map data) {
        // Do nothing...
        return this;
    }
}
