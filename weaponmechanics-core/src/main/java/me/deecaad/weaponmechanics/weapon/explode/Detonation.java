package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.MechanicManager;
import me.deecaad.core.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Detonation implements Serializer<Detonation> {

    private Set<ExplosionTrigger> triggers;
    private int delay;
    private boolean removeProjectileOnDetonation;
    private MechanicManager impactMechanics;

    /**
     * Default constructor for serializer
     */
    public Detonation() {
    }

    public Detonation(Set<ExplosionTrigger> triggers, int delay, boolean removeProjectileOnDetonation, MechanicManager impactMechanics) {
        this.triggers = triggers;
        this.delay = delay;
        this.removeProjectileOnDetonation = removeProjectileOnDetonation;
        this.impactMechanics = impactMechanics;
    }

    public Set<ExplosionTrigger> getTriggers() {
        return triggers;
    }

    public int getDelay() {
        return delay;
    }

    public boolean isRemoveProjectileOnDetonation() {
        return removeProjectileOnDetonation;
    }

    public MechanicManager getImpactMechanics() {
        return impactMechanics;
    }

    @Override
    @NotNull public Detonation serialize(@NotNull SerializeData data) throws SerializerException {

        Set<ExplosionTrigger> triggers = new HashSet<>(ExplosionTrigger.values().length, 1.0f);
        for (ExplosionTrigger trigger : ExplosionTrigger.values()) {
            String key = StringUtil.snakeToUpperSnake(trigger.name().toLowerCase(Locale.ROOT));
            boolean enable = data.of("Impact_When." + key).getBool().orElse(false);

            if (enable)
                triggers.add(trigger);
        }

        // Time after the trigger the explosion occurs
        int delay = data.of("Delay_After_Impact").assertRange(1, null).getInt().orElse(1);
        boolean removeProjectileOnDetonation = data.of("Remove_Projectile_On_Detonation").getBool().orElse(true);

        // Impact mechanics
        MechanicManager impactMechanics = data.of("Impact_Mechanics").serialize(MechanicManager.class).orElse(null);

        return new Detonation(triggers, delay, removeProjectileOnDetonation, impactMechanics);
    }
}
