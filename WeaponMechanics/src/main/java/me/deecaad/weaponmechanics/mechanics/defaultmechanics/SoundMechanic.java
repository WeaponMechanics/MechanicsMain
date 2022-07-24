package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerEnumException;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmSound;
import me.deecaad.weaponmechanics.weapon.reload.ReloadSound;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.getPlugin;

public class SoundMechanic implements IMechanic<SoundMechanic> {

    // While [0, 0.5] is technically a valid range, all of those values are
    // treated the same as 0.5.
    private static final float MIN_PITCH = 0.5f;
    private static final float MAX_PITCH = 2.0f;

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
    @Nonnull
    public SoundMechanic serialize(SerializeData data) throws SerializerException {

        // Expecting <Sound>-<Volume>-<Pitch>-<Delay>-<Noise>
        List<String[]> stringSoundList = data.ofList()
                .addArgument(Sound.class, true, true)
                .addArgument(double.class, true).assertArgumentPositive()
                .addArgument(double.class, false).assertArgumentRange(0.5, 2.0)
                .addArgument(int.class, false).assertArgumentPositive()
                .addArgument(double.class, false).assertArgumentRange(0.0, 1.0)
                .addArgument(net.kyori.adventure.sound.Sound.Source.class, false)
                .assertList().assertExists().get();

        List<SoundMechanicData> soundList = new ArrayList<>();
        int delayedCounter = 0;

        for (int i = 0; i < stringSoundList.size(); i++) {
            String[] split = stringSoundList.get(i);

            float volume = Float.parseFloat(split[1]);
            float pitch = split.length > 2 ? Float.parseFloat(split[2]) : 1.0f;
            int delay = split.length > 3 ? Integer.parseInt(split[3]) : 0;
            float noise = split.length > 4 ? Float.parseFloat(split[4]) : 0.0f;
            net.kyori.adventure.sound.Sound.Source category = split.length > 5 ? net.kyori.adventure.sound.Sound.Source.valueOf(split[5]) : net.kyori.adventure.sound.Sound.Source.PLAYER;

            if (delay > 0)
                delayedCounter++;

            // Adventure API can play named sounds with a bunch of options, so
            // we prefer to play custom sounds. 1.16+, all sounds are
            // to be custom sounds.
            String stringSound = split[0].trim();
            if (stringSound.toLowerCase().startsWith("custom:")) {
                stringSound = stringSound.substring("custom:".length());
                soundList.add(new CustomSound(stringSound, volume, pitch, delay, noise, category));
                continue;
            }

            try {
                Sound sound = Sound.valueOf(stringSound);

                if (ReflectionUtil.getMCVersion() >= 16)
                    soundList.add(new CustomSound(sound.getKey().getKey(), volume, pitch, delay, noise, category));
                else
                    soundList.add(new BukkitSound(sound, volume, pitch, delay, noise, category));

            } catch (IllegalArgumentException e) {
                throw new SerializerEnumException(this, Sound.class, stringSound, false, data.ofList().getLocation(i));
            }
        }

        return new SoundMechanic(delayedCounter, soundList);
    }

    public static abstract class SoundMechanicData {

        private final float volume;
        private final float pitch;
        private final int delay;
        private final float noise;
        private final net.kyori.adventure.sound.Sound.Source category;

        public SoundMechanicData(float volume, float pitch, int delay, float noise, net.kyori.adventure.sound.Sound.Source category) {
            this.volume = volume;
            this.pitch = pitch;
            this.delay = delay;
            this.noise = noise;
            this.category = category;
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

        @Nonnull
        public net.kyori.adventure.sound.Sound.Source getCategory() {
            return category;
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

        public BukkitSound(Sound sound, float volume, float pitch, int delay, float noise, net.kyori.adventure.sound.Sound.Source category) {
            super(volume, pitch, delay, noise, category);
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

        public CustomSound(String sound, float volume, float pitch, int delay, float noise, net.kyori.adventure.sound.Sound.Source category) {
            super(volume, pitch, delay, noise, category);
            this.sound = sound;
        }

        @Override
        public void play(CastData castData) {
            World world = castData.getCastLocation().getWorld();
            net.kyori.adventure.sound.Sound adventure = net.kyori.adventure.sound.Sound.sound(Key.key(sound), getCategory(), getVolume(), getRandomPitch());
            Audience audience = MechanicsCore.getPlugin().adventure.world(Key.key(world.getName()));

            // If there is an entity involved, we can play the sound from the
            // entity as a moving sound. In 1.15 and 1.16, the pitch/volume will
            // be broken, but that is a small price to pay for sounds that
            // properly track the shooter.
            LivingEntity caster = castData.getCaster();
            if (caster == null) {
                Location loc = castData.getCastLocation();
                audience.playSound(adventure, loc.getX(), loc.getY(), loc.getZ());
            } else {
                audience.playSound(adventure, (net.kyori.adventure.sound.Sound.Emitter) caster);
            }
        }
    }
}
