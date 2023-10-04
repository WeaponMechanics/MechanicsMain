package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Detonation implements Serializer<Detonation> {

    private Set<ExplosionTrigger> triggers;
    private int delay;
    private boolean removeProjectileOnDetonation;

    /**
     * Default constructor for serializer
     */
    public Detonation() {
    }

    public Detonation(Set<ExplosionTrigger> triggers, int delay, boolean removeProjectileOnDetonation) {
        this.triggers = triggers;
        this.delay = delay;
        this.removeProjectileOnDetonation = removeProjectileOnDetonation;
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

    @Override
    @NotNull
    public Detonation serialize(@NotNull SerializeData data) throws SerializerException {

        Set<ExplosionTrigger> triggers = new HashSet<>(ExplosionTrigger.values().length, 1.0f);
        for (ExplosionTrigger trigger : ExplosionTrigger.values()) {
            String key = StringUtil.upperSnakeCase(trigger.name().toLowerCase(Locale.ROOT));
            boolean enable = data.of("Impact_When." + key).getBool(false);

            if (enable)
                triggers.add(trigger);
        }

        // Time after the trigger the explosion occurs
        int delay = data.of("Delay_After_Impact").assertPositive().getInt(0);
        boolean removeProjectileOnDetonation = data.of("Remove_Projectile_On_Detonation").getBool(true);

        return new Detonation(triggers, delay, removeProjectileOnDetonation);
    }
}
