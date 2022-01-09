package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;

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
    public Detonation serialize(File file, ConfigurationSection configurationSection, String path) {

        ConfigurationSection impactWhenSection = configurationSection.getConfigurationSection(path + ".Impact_When");
        if (impactWhenSection == null) {
            return null;
        }

        Set<ExplosionTrigger> triggers = new HashSet<>(4, 1.0f);
        for (String key : impactWhenSection.getKeys(false)) {
            try {
                ExplosionTrigger trigger = ExplosionTrigger.valueOf(key.toUpperCase());
                boolean value = impactWhenSection.getBoolean(key);

                if (value) triggers.add(trigger);
            } catch (IllegalArgumentException ex) {
                debug.log(LogLevel.ERROR,
                        StringUtil.foundInvalid("trigger type"),
                        StringUtil.foundAt(file, path + ".Impact_When", key),
                        StringUtil.debugDidYouMean(key, ExplosionTrigger.class));
                return null;
            }
        }

        // Time after the trigger the explosion occurs
        int delay = configurationSection.getInt(path + ".Delay_After_Impact");
        debug.validate(delay >= 0, "Delay should be positive", StringUtil.foundAt(file, path + ".Delay_After_Impact"));

        boolean removeProjectileOnDetonation = configurationSection.getBoolean(path + ".Remove_Projectile_On_Detonation");

        return new Detonation(triggers, delay, removeProjectileOnDetonation);
    }
}
