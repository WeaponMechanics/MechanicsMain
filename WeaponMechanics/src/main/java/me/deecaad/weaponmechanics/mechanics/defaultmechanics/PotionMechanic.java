package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class PotionMechanic implements IMechanic<PotionMechanic> {

    private List<PotionEffect> potionEffectList;

    /**
     * Empty constructor to be used as serializer
     */
    public PotionMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), this);
    }

    public PotionMechanic(List<PotionEffect> potionEffectList) {
        this.potionEffectList = potionEffectList;
    }

    @Override
    public void use(CastData castData) {
        castData.getCaster().addPotionEffects(this.potionEffectList);
    }

    @Override
    public boolean requireEntity() {
        return true;
    }

    @Override
    public String getKeyword() {
        return "Potion_Effects";
    }

    @Override
    public PotionMechanic serialize(File file, ConfigurationSection configurationSection, String path) {
        List<String> stringPotionList = configurationSection.getStringList(path);
        if (stringPotionList == null || stringPotionList.isEmpty()) return null;

        List<PotionEffect> potionEffectList = new ArrayList<>();

        for (String stringInList : stringPotionList) {
            for (String stringInLine : stringInList.split(", ?")) {

                String[] potionData = StringUtil.split(stringInLine);

                if (potionData.length < 3) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid potion format in configurations!",
                            "Located at file " + file + " in " + path + " (" + stringInLine + ") in configurations",
                            "At least these are required <PotionEffectType>-<duration in ticks>-<amplifier>");
                    continue;
                }

                int duration;
                int amplifier;
                boolean allowParticles = true;
                boolean produceMoreParticles = false;
                boolean icon = false;

                try {
                    duration = Integer.parseInt(potionData[1]);
                    amplifier = Integer.parseInt(potionData[2]);
                } catch (NumberFormatException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid number format in configurations!",
                            "Located at file " + file + " in " + path + " (" + stringInLine + ") in configurations",
                            "Make sure you use numbers in duration and amplifier args.");
                    continue;
                }

                if (duration < 0) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid number in configurations!",
                            "Located at file " + file + " in " + path + " (" + stringInLine + ") in configurations",
                            "Make sure duration is positive number. 20 ticks equals 1 second.");
                    continue;
                }
                if (amplifier < 1) amplifier = 1;

                if (potionData.length > 3) allowParticles = Boolean.parseBoolean(potionData[3]);
                if (potionData.length > 4) produceMoreParticles = Boolean.parseBoolean(potionData[4]);
                if (potionData.length > 5) icon = Boolean.parseBoolean(potionData[5]);

                try {
                    PotionEffectType potionEffectType = PotionEffectType.getByName(potionData[0]);
                    if (CompatibilityAPI.getVersion() < 1.14) {
                        potionEffectList.add(new PotionEffect(potionEffectType, duration, amplifier, produceMoreParticles, allowParticles));
                    } else {
                        potionEffectList.add(new PotionEffect(potionEffectType, duration, amplifier, produceMoreParticles, allowParticles, icon));
                    }
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid potion effect type in configurations!",
                            "Located at file " + file + " in " + path + " (" + stringInLine + ") in configurations");
                }
            }
        }

        if (potionEffectList.isEmpty()) return null;
        return new PotionMechanic(potionEffectList);
    }
}