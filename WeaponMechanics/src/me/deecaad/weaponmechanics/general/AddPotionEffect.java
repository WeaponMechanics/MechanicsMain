package me.deecaad.weaponmechanics.general;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class AddPotionEffect implements Serializer<AddPotionEffect> {

    private List<PotionEffect> potionEffects;

    /**
     * Empty constructor to be used as serializer
     */
    public AddPotionEffect() { }

    public AddPotionEffect(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    /**
     * Adds all potion effects held in this object to living entity
     *
     * @param livingEntity the receiving living entity
     */
    public void add(LivingEntity livingEntity) {
        livingEntity.addPotionEffects(potionEffects);
    }

    @Override
    public String getKeyword() {
        return "Potion_Effect";
    }

    @Override
    public AddPotionEffect serialize(File file, ConfigurationSection configurationSection, String path) {
        List<String> stringPotionEffects = configurationSection.getStringList(path + ".Potion_Effects");
        if (stringPotionEffects == null) return null;

        boolean particles = configurationSection.getBoolean(path + ".Allow_Particles", false);
        boolean ambient = configurationSection.getBoolean(path + ".Produce_More_Particles", false);

        List<PotionEffect> potionEffects = new ArrayList<>();
        for (String stringPotionEffect : stringPotionEffects) {
            String[] splittedPotionEffect = StringUtils.split(stringPotionEffect);
            if (splittedPotionEffect.length < 3) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid potion effect format in configurations!",
                        "Located at file " + file + " in " + path + ".Potion_Effects (" + stringPotionEffect + ") in configurations",
                        "Correct format is <PotionEffectType>-<duration in ticks>-<amplifier>");
                continue;
            }
            PotionEffectType potionEffectType = PotionEffectType.getByName(splittedPotionEffect[0]);
            if (potionEffectType == null) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid potion effect type in configurations!",
                        "Located at file " + file + " in " + path + ".Potion_Effects (" + stringPotionEffect + ") in configurations");
                continue;
            }

            int duration;
            int amplifier;
            try {
                duration = Integer.parseInt(splittedPotionEffect[1]);

                // -1 because amplifier 0 basically means 1, 1 means 2 and so on...
                // Now when defining potion effect amplifier 1 actually means potion effect level 1 (and not 2 level)
                amplifier = Integer.parseInt(splittedPotionEffect[2]) - 1;
            } catch (NumberFormatException e) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid potion effect duration or amplifier in configurations!",
                        "Located at file " + file + " in " + path + ".Potion_Effects (" + stringPotionEffect + ") in configurations",
                        "Make sure they're integers e.g. 1, 5, 8, 23");
                continue;
            }
            potionEffects.add(new PotionEffect(potionEffectType, duration, amplifier, ambient, particles));
        }
        if (potionEffects.isEmpty()) {
            return null;
        }

        return new AddPotionEffect(potionEffects);
    }
}