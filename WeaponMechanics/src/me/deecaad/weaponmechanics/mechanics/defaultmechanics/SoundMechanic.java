package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.SoundHelper;
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class SoundMechanic implements Serializer<SoundMechanic>, IMechanic {

    private static final float MIN_PITCH = (float) 0.0;
    private static final float MAX_PITCH = (float) 2.0;
    private static Method worldGetHandle;
    private static Method makeSoundMethod;

    static {
        if (CompatibilityAPI.getVersion() < 1.09) {
            worldGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftWorld"), "getHandle");
            makeSoundMethod = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("World"), "makeSound", double.class, double.class, double.class, String.class, float.class, float.class);
        }
    }

    private boolean hasDelay;
    private List<SoundMechanicData> soundList;

    /**
     * Empty constructor to be used as serializer
     */
    public SoundMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), getKeyword());
    }

    public SoundMechanic(boolean hasDelay, List<SoundMechanicData> soundList) {
        this.hasDelay = hasDelay;
        this.soundList = soundList;
    }

    @Override
    public void use(CastData castData) {
        if (!hasDelay) {
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
                castData.getCasterWrapper().getMainHandData().addReloadTask(startWithDelays(castData));
            } else {
                castData.getCasterWrapper().getOffHandData().addReloadTask(startWithDelays(castData));
            }
        } else if (firearmActionData != null) {
            if (firearmActionData == FirearmSound.MAIN_HAND.getId()) {
                castData.getCasterWrapper().getMainHandData().addFirearmActionTask(startWithDelays(castData));
            } else {
                castData.getCasterWrapper().getOffHandData().addFirearmActionTask(startWithDelays(castData));
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
    public int startWithDelays(CastData castData) {
        if (!hasDelay) return 0;

        // Sound list is in order from 0 delay to longest delay
        Iterator<SoundMechanicData> iterator = soundList.iterator();

        // Only play sounds which should be played instantly
        while (iterator.hasNext()) {

            SoundMechanicData next = iterator.next();

            // If delay doesn't match 0 -> break
            if (next.getDelay() != 0) break;

            next.play(castData);
        }

        return new BukkitRunnable() {

            // Since this will be actually ran in the next tick
            int ticker = 1;

            @Override
            public void run() {

                while (iterator.hasNext()) {

                    SoundMechanicData next = iterator.next();

                    // If delay doesn't match ticks since start -> break
                    if (next.getDelay() != ticker) break;

                    next.play(castData);
                }

                if (!iterator.hasNext()) {
                    cancel();
                    return;
                }

                ++ticker;
            }
        }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 0, 0).getTaskId();
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

        boolean hasDelay = false;

        for (String stringInList : stringSoundList) {
            for (String stringInLine : stringInList.split(", ?")) {

                String[] soundData = StringUtils.split(stringInLine);

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

                    if (soundData.length > 3) delay = Integer.parseInt(soundData[3]);
                    if (delay > 0) hasDelay = true;

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
                    Sound sound = SoundHelper.fromStringToSound(stringSound);
                    soundList.add(new BukkitSound(sound, volume, pitch, delay, noise));
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid bukkit sound in configurations!",
                            "Located at file " + file + " in " + path + " (" + stringInLine + ") in configurations");
                }
            }
        }
        if (soundList.isEmpty()) return null;

        if (hasDelay) {

            // Sort list to be from 0 to max
            soundList.sort(Comparator.comparingInt(SoundMechanicData::getDelay));
        }

        return new SoundMechanic(hasDelay, soundList);
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

            float noise = (float) NumberUtils.random(-this.noise, this.noise);
            float pitch = this.pitch + noise;

            return NumberUtils.minMax(MIN_PITCH, pitch, MAX_PITCH);
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
