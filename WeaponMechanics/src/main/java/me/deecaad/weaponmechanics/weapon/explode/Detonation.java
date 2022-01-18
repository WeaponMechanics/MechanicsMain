package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Detonation implements Serializer<Detonation> {

    private Set<ExplosionTrigger> triggers;
    private int delay;
    private boolean removeProjectileOnDetonation;

    public Detonation() { }

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
    public String getKeyword() {
        return "Detonation";
    }

    @Override
    @Nonnull
    public Detonation serialize(SerializeData data) throws SerializerException {

        Set<ExplosionTrigger> triggers = new HashSet<>(ExplosionTrigger.values().length, 1.0f);
        for (ExplosionTrigger trigger : ExplosionTrigger.values()) {
            String key = StringUtil.upperSnakeCase(trigger.name());
            boolean enable = data.of(key).assertType(Boolean.class).get(false);

            if (enable)
                triggers.add(trigger);
        }

        // Time after the trigger the explosion occurs
        int delay = data.of("Delay_After_Impact").assertPositive().get(0);
        boolean removeProjectileOnDetonation = data.of("Remove_Projectile_On_Detonation").assertType(Boolean.class).get(true);

        return new Detonation(triggers, delay, removeProjectileOnDetonation);
    }
}
