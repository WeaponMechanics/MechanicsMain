package me.deecaad.weaponmechanics.general;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.SoundHelper;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class PlaySound implements Serializer<PlaySound> {

    private List<SoundData> sounds;

    /**
     * Reflection support for 1.8 string sounds
     */
    private static Method worldGetHandle;
    private static Method makeSoundMethod;

    /**
     * Empty constructor to be used as serializer
     */
    public PlaySound() {
        if (CompatibilityAPI.getVersion() < 1.09) {
            if (worldGetHandle == null) {
                worldGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftWorld"), "getHandle");
            }
            if (makeSoundMethod == null) {
                makeSoundMethod = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("World"), "makeSound", double.class, double.class, double.class, String.class, float.class, float.class);
            }
        }
    }

    public PlaySound(List<SoundData> sounds, List<SoundData> customSounds) {
        this.sounds = new ArrayList<>();
        this.sounds.addAll(sounds);
        this.sounds.addAll(customSounds);
    }

    /**
     * @param entity the entity to whose location sounds are going to be played
     */
    public void play(Entity entity) {
        for (SoundData sound : sounds) {
            if (sound.delay < 1) {
                play(entity.getLocation(), sound);
                continue;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    play(entity.getLocation(), sound);
                }
            }.runTaskLater(WeaponMechanics.getPlugin(), sound.delay);
        }
    }

    /**
     * @param location the location where to play sounds
     */
    public void play(Location location) {
        for (SoundData sound : sounds) {
            if (sound.delay < 1) {
                play(location, sound);
                continue;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    play(location, sound);
                }
            }.runTaskLater(WeaponMechanics.getPlugin(), sound.delay);
        }
    }

    /**
     * If you want to cancel sounds from playing later on remember to store all integers returned by this method.
     *
     * @param entity the entity to whose location sounds are going to be played
     * @return the list of used tasks (can't return null)
     */
    public List<Integer> playAndSaveTasks(Entity entity) {
        List<Integer> tasks = new ArrayList<>();
        for (SoundData sound : sounds) {
            if (sound.delay < 1) {
                play(entity.getLocation(), sound);
                continue;
            }
            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    play(entity.getLocation(), sound);
                }
            }.runTaskLater(WeaponMechanics.getPlugin(), sound.delay).getTaskId());
        }
        return tasks;
    }

    /**
     * Plays the sound to given location without using delay
     *
     * @param location the location to spawn
     * @param sound  the sound data to play
     */
    private void play(Location location, SoundData sound) {
        if (CompatibilityAPI.getVersion() >= 1.11) {
            if (sound.isCustomSound()) {
                location.getWorld().playSound(location, sound.customSound, SoundCategory.PLAYERS, sound.volume, sound.pitch);
            } else {
                location.getWorld().playSound(location, sound.sound, SoundCategory.PLAYERS, sound.volume, sound.pitch);
            }
        } else {
            if (!sound.isCustomSound()) {
                location.getWorld().playSound(location, sound.sound, sound.volume, sound.pitch);
            } else if (CompatibilityAPI.getVersion() >= 1.09) {
                location.getWorld().playSound(location, sound.customSound, sound.volume, sound.pitch);
            } else {
                Object worldServer = ReflectionUtil.invokeMethod(worldGetHandle, location.getWorld());
                ReflectionUtil.invokeMethod(makeSoundMethod, worldServer, location.getX(), location.getY(), location.getZ(), sound.customSound, sound.volume, sound.pitch);
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Sound";
    }

    @Override
    public PlaySound serialize(File file, ConfigurationSection configurationSection, String path) {
        List<SoundData> bukkitSounds = tryListSoundData(file, configurationSection, path, true);
        List<SoundData> customSounds = tryListSoundData(file, configurationSection, path, false);
        if (bukkitSounds == null && customSounds == null) {
            return null;
        }
        return new PlaySound(bukkitSounds, customSounds);
    }

    public List<SoundData> tryListSoundData(File file, ConfigurationSection configurationSection, String path, boolean isBukkit) {
        String listName = (isBukkit ? "Bukkit_Sounds" : "Custom_Sounds");
        List<?> soundList = configurationSection.getList(path + "." + listName);
        if (soundList == null) {
            return null;
        }
        List<SoundData> sounds = new ArrayList<>();
        for (Object sound : soundList) {

            String[] splitDots = sound.toString().split(",");

            for (String oneSound : splitDots) {
                String[] splitted = StringUtils.split(oneSound);
                if (splitted.length < 4) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid sound format in configurations!",
                            "Located at file " + file + " in " + path + "." + listName + " (" + oneSound + ") in configurations",
                            "Correct format is <sound>-<volume>-<pitch>-<delay>");
                    continue;
                }
                float volume;
                float pitch;
                int delay;
                try {
                    volume = Float.parseFloat(splitted[1]);
                    pitch = Float.parseFloat(splitted[2]);
                    delay = Integer.parseInt(splitted[3]);
                } catch (NumberFormatException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid sound format in configurations!",
                            "Located at file " + file + " in " + path + "." + listName + " (" + oneSound + ") in configurations",
                            "Correct format is <sound>-<volume>-<pitch>-<delay>");
                    continue;
                }
                if (isBukkit) {
                    Sound bukkitSound;
                    try {
                        bukkitSound = SoundHelper.fromStringToSound(splitted[0].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        debug.log(LogLevel.ERROR,
                                "Found an invalid sound in configurations!",
                                "Located at file " + file + " in " + path + "." + listName + " (" + splitted[0].toUpperCase() + ") in configurations");
                        continue;
                    }
                    sounds.add(new SoundData(bukkitSound, volume, pitch, delay));
                } else {
                    sounds.add(new SoundData(splitted[0], volume, pitch, delay));
                }
            }
        }
        return sounds;
    }

    public static class SoundData {

        private Sound sound;
        private String customSound;
        private float volume;
        private float pitch;
        private int delay;

        public SoundData(Sound sound, float volume, float pitch, int delay) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.delay = delay;
        }

        public SoundData(String customSound, float volume, float pitch, int delay) {
            this.customSound = customSound;
            this.volume = volume;
            this.pitch = pitch;
            this.delay = delay;
        }

        public boolean isCustomSound() {
            return this.customSound != null;
        }
    }
}