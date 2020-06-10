package me.deecaad.core.effects.serializers;

import me.deecaad.core.effects.Effect;
import me.deecaad.core.effects.types.CustomSoundEffect;
import me.deecaad.core.effects.types.MinecraftSoundEffect;
import me.deecaad.core.effects.types.SoundEffect;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.core.MechanicsCore.debug;

public class SoundEffectSerializer implements Serializer<List<SoundEffect>> {

    public SoundEffectSerializer() {
    }

    @Override
    public String getKeyword() {
        return "Sounds";
    }

    @Override
    public List<SoundEffect> serialize(File file, ConfigurationSection configurationSection, String path) {
        List<String> strings = configurationSection.getStringList(path);
        List<SoundEffect> effects = new ArrayList<>(strings.size());

        String location = "Found in file " + file + " at path " + path;

        for (String str: strings) {
            String[] split = str.split(", *");

            for (String soundData: split) {

                // This will be in the format sound~volume~pitch~delay
                String[] data = StringUtils.split(soundData);

                if (data.length < 3) {
                    debug.error("Found an invalid sound format! Use <sound>~<volume>~<pitch>~<delay>~<pitch-noise>", location);
                    debug.debug("Invalid sound: " + soundData);
                    continue;
                }

                String sound = data[0];
                float volume;
                float pitch;
                int delay;
                float noise;

                try {
                    volume = Float.parseFloat(data[1]);
                    pitch = Float.parseFloat(data[2]);
                    delay = data.length > 3 ? Integer.parseInt(data[3]) : 0;
                    noise = data.length > 4 ? Float.parseFloat(data[4]) : 0;

                } catch(NumberFormatException ex) {
                    debug.error("Found an invalid number format! Make sure you you use only numbers!", location);
                    debug.log(LogLevel.DEBUG, ex);
                    continue;
                }

                debug.validate(volume >= 0, "Volume must be positive! Found: " + volume, location);
                debug.validate(pitch >= 0 && pitch <= 2, "Pitch needs to be within bounds [0, 2] (So 0 <= pitch <= 2)! Found: " + pitch, location);
                debug.validate(delay >= 0, "Delay MUST be positive! Found: " + delay, location);
                debug.validate(noise >= 0, "Pitch-Noise must be positive! Found: " + noise, location);

                SoundEffect effect;

                // Custom named sound for sounds from resource packs
                if (sound.toLowerCase().startsWith("custom:")) {
                    sound = sound.substring("custom:".length());

                    effect = new CustomSoundEffect(sound, volume, pitch, noise);

                } else {
                    if (sound.toLowerCase().startsWith("minecraft:")) {
                        sound = sound.substring("minecraft:".length());
                    }

                    try {
                        effect = new MinecraftSoundEffect(Sound.valueOf(sound), volume, pitch, noise);
                    } catch (EnumConstantNotPresentException ex) {
                        debug.error("Unknown sound: " + sound, location);
                        debug.log(LogLevel.DEBUG, ex);
                        continue;
                    }
                }

                effect.setDelay(delay);
                effects.add(effect);
            }
        }

        return effects;
    }
}
