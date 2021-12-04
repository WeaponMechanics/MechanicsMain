package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.SoundUtil;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmSound;
import me.deecaad.weaponmechanics.weapon.reload.ReloadSound;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getPlugin;

public class SoundMechanic implements IMechanic<SoundMechanic> {

    // While [0, 0.5] is technically a valid range, all of those values are
    // treated the same as 0.5.
    private static final float MIN_PITCH = 0.5f;
    private static final float MAX_PITCH = 2.0f;
    private static Method worldGetHandle;
    private static Method makeSoundMethod;

    static {
        if (CompatibilityAPI.getVersion() < 1.09) {
            worldGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftWorld"), "getHandle");
            makeSoundMethod = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("world.level", "World"), "makeSound", double.class, double.class, double.class, String.class, float.class, float.class);
        }
    }

    private int delayCounter;
    private List<SoundMechanicData> soundList;

    /**
     * Empty constructor to be used as serializer
     */
    public SoundMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), this);
    }

    public SoundMechanic(int delayCounter, List<SoundMechanicData> soundList) {
        this.delayCounter = delayCounter;
        this.soundList = soundList;
    }

    @Override
    public void use(CastData castData) {
        if (delayCounter == 0) {
            for (SoundMechanicData soundMechanicData : soundList) {
                soundMechanicData.play(castData);
            }
            return;
        }


        // Check if this is start reload cast
        Integer reloadData = castData.getData(ReloadSound.getDataKeyword(), Integer.class);
        Integer firearmActionData = castData.getData(FirearmSound.getDataKeyword(), Integer.class);
        
        if (reloadData != null) {
            if (reloadData == ReloadSound.MAIN_HAND.getId()) {
                castData.getCasterWrapper().getMainHandData().addReloadTasks(startWithDelays(castData));
            } else {
                castData.getCasterWrapper().getOffHandData().addReloadTasks(startWithDelays(castData));
            }
        } else if (firearmActionData != null) {
            if (firearmActionData == FirearmSound.MAIN_HAND.getId()) {
                castData.getCasterWrapper().getMainHandData().addFirearmActionTasks(startWithDelays(castData));
            } else {
                castData.getCasterWrapper().getOffHandData().addFirearmActionTasks(startWithDelays(castData));
            }
        } else {
            startWithDelays(castData);
        }
    }

    /**
     *
     * @param castData the cast data
     * @return the task id chain of delayed sound plays and 0 if not used
     */
    public int[] startWithDelays(CastData castData) {

        int[] tasks = new int[delayCounter];
        int counter = 0;
        for (SoundMechanicData sound : soundList) {

            // For sounds with 0 delay, it is important that we play them
            // without using the scheduler, otherwise they will take up
            // unnecessary resources, and will be played 1 tick late.
            if (sound.delay == 0) {
                sound.play(castData);
            } else {
                int task = (new BukkitRunnable() {
                    @Override
                    public void run() {
                        sound.play(castData);
                    }
                }.runTaskLater(getPlugin(), sound.delay).getTaskId());

                tasks[counter++] = task;
            }
        }

        return tasks;
    }

    @Override
    public String getKeyword() {
        return "Sounds";
    }

    @Override
    public SoundMechanic serialize(File file, ConfigurationSection configurationSection, String path) {
        List<String> stringSoundList = configurationSection.getStringList(path);
        if (stringSoundList == null || stringSoundList.isEmpty()) return null;

        List<SoundMechanicData> soundList = new ArrayList<>();

        int delayedCounter = 0;

        for (String stringInList : stringSoundList) {
            for (String stringInLine : stringInList.split(", ?")) {

                String[] soundData = StringUtil.split(stringInLine);

                if (soundData.length < 3) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid sound format in configurations!",
                            "Located at file " + file + " in " + path + " (" + stringInLine + ") in configurations",
                            "At least these are required <sound>-<volume>-<pitch>");
                    continue;
                }

                float volume;
                float pitch;
                int delay = 0;
                float noise = 0.0f;

                try {
                    volume = Float.parseFloat(soundData[1]);
                    pitch = Float.parseFloat(soundData[2]);

                    if (pitch < 0.0 || pitch > 2.0) {
                        debug.log(LogLevel.ERROR,
                                StringUtil.foundInvalid("pitch"),
                                StringUtil.foundAt(file, path, stringInLine),
                                "Make sure that pitch is between 0.5 and 2.0.");
                        return null;
                    }

                    pitch = Math.max(pitch, 0.5f);

                    if (soundData.length > 3) delay = Integer.parseInt(soundData[3]);
                    if (delay > 0) delayedCounter++;

                    if (soundData.length > 4) noise = Float.parseFloat(soundData[4]);
                } catch (NumberFormatException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid number format in configurations!",
                            "Located at file " + file + " in " + path + " (" + stringInLine + ") in configurations",
                            "Make sure you only use numbers after sound parameter.");
                    continue;
                }
                if (delay < 0) delay = 0;
                if (noise < 0) noise = 0;

                String stringSound = soundData[0];
                if (stringSound.toLowerCase().startsWith("custom:")) {
                    stringSound = stringSound.substring("custom:".length());
                    soundList.add(new CustomSound(stringSound, volume, pitch, delay, noise));
                    continue;
                }

                try {
                    Sound sound = SoundUtil.fromStringToSound(stringSound);
                    soundList.add(new BukkitSound(sound, volume, pitch, delay, noise));
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            StringUtil.foundInvalid("bukkit sound"),
                            StringUtil.foundAt(file, path, stringInLine),
                            StringUtil.debugDidYouMean(stringSound.toUpperCase(), Sound.class));
                    debug.log(LogLevel.ERROR,
                            "Found an invalid bukkit sound in configurations!",
                            "Located at file " + file + " in " + path + " (" + stringInLine + ") in configurations");
                }
            }
        }
        if (soundList.isEmpty()) return null;

        return new SoundMechanic(delayedCounter, soundList);
    }

    public static abstract class SoundMechanicData {

        private final float volume;
        private final float pitch;
        private final int delay;
        private final float noise;

        public SoundMechanicData(float volume, float pitch, int delay, float noise) {
            this.volume = volume;
            this.pitch = pitch;
            this.delay = delay;
            this.noise = noise;
        }

        public abstract void play(CastData castData);

        public float getVolume() {
            return volume;
        }

        public float getPitch() {
            return pitch;
        }

        public int getDelay() {
            return delay;
        }

        public float getRandomPitch() {
            if (noise == 0.0f) return this.pitch;

            // There is no method to generate a random float in a range, so we
            // take the negligible performance impact for generating twice
            // the amount of data.
            float noise = (float) NumberUtil.random(-this.noise, this.noise);
            float pitch = this.pitch + noise;

            return NumberUtil.minMax(MIN_PITCH, pitch, MAX_PITCH);
        }
    }

    public static class BukkitSound extends SoundMechanicData {

        private final Sound sound;

        public BukkitSound(Sound sound, float volume, float pitch, int delay, float noise) {
            super(volume, pitch, delay, noise);
            this.sound = sound;
        }

        @Override
        public void play(CastData castData) {
            Location castLocation = castData.getCastLocation();
            if (CompatibilityAPI.getVersion() >= 1.11) {
                castLocation.getWorld().playSound(castLocation, sound, SoundCategory.PLAYERS, getVolume(), getRandomPitch());
            } else {
                castLocation.getWorld().playSound(castLocation, sound, getVolume(), getRandomPitch());
            }
        }
    }

    public static class CustomSound extends SoundMechanicData {

        private final String sound;

        public CustomSound(String sound, float volume, float pitch, int delay, float noise) {
            super(volume, pitch, delay, noise);
            this.sound = sound;
        }

        @Override
        public void play(CastData castData) {
            Location castLocation = castData.getCastLocation();
            if (CompatibilityAPI.getVersion() >= 1.11) {
                castLocation.getWorld().playSound(castLocation, sound, SoundCategory.PLAYERS, getVolume(), getRandomPitch());
            } else if (CompatibilityAPI.getVersion() >= 1.09) {
                castLocation.getWorld().playSound(castLocation, sound, getVolume(), getRandomPitch());
            } else {
                Object worldServer = ReflectionUtil.invokeMethod(worldGetHandle, castLocation.getWorld());
                double x = castLocation.getX(), y = castLocation.getY(), z = castLocation.getZ();
                ReflectionUtil.invokeMethod(makeSoundMethod, worldServer, x, y, z, sound, getVolume(), getRandomPitch());
            }
        }
    }
}
