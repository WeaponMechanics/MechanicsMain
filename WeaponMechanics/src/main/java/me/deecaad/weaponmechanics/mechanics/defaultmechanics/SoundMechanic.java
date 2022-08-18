package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerEnumException;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmSound;
import me.deecaad.weaponmechanics.weapon.reload.ReloadSound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.*;

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
    public boolean shouldSerialize(SerializeData data) {
        // Let Mechanics handle the auto serializer
        return false;
    }

    protected SoundMechanicData serialize(SerializeData data, Sound sound, float volume, float pitch, int delay, float noise, double distance, MaterialCategory mat, String category) throws SerializerException {
        return new BukkitSound(sound, volume, pitch, delay, noise, distance, mat, getSoundCategory(data, category));
    }

    protected SoundMechanicData serialize(SerializeData data, String sound, float volume, float pitch, int delay, float noise, double distance, MaterialCategory mat, String category) throws SerializerException {
        return new CustomSound(sound, volume, pitch, delay, noise, distance, mat, getSoundCategory(data, category));
    }

    public String getSoundCategory(SerializeData data, String category) throws SerializerException {
        if (category == null || "NULL".equals(category))
            category = "PLAYERS";

        if (ReflectionUtil.getMCVersion() >= 11) {
            Optional<SoundCategory> optional = EnumUtil.getIfPresent(SoundCategory.class, category);
            if (!optional.isPresent())
                throw new SerializerEnumException(this, SoundCategory.class, category, false, data.of().getLocation());
        }

        return category;
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
                .addArgument(double.class, false).assertArgumentPositive()
                .addArgument(MaterialCategory.class, false)
                .addArgument(String.class, false, true) // Cannot use SoundCategory since it didn't exist
                .assertList().assertExists().get();

        List<SoundMechanicData> soundList = new ArrayList<>();
        int delayedCounter = 0;

        for (int i = 0; i < stringSoundList.size(); i++) {
            String[] split = stringSoundList.get(i);

            float volume = Float.parseFloat(split[1]);
            float pitch = split.length > 2 ? Float.parseFloat(split[2]) : 1.0f;
            int delay = split.length > 3 ? Integer.parseInt(split[3]) : 0;
            float noise = split.length > 4 ? Float.parseFloat(split[4]) : 0.0f;
            double distance = split.length > 5 ? Double.parseDouble(split[5]) : Double.NaN;
            MaterialCategory mat = split.length > 6 ? MaterialCategory.valueOf(split[6].toUpperCase(Locale.ROOT)) : null;
            String category = split.length > 7 ? split[7].toUpperCase(Locale.ROOT) : null;

            if (delay > 0)
                delayedCounter++;

            String stringSound = split[0].trim();
            if (stringSound.toLowerCase().startsWith("custom:")) {
                stringSound = stringSound.substring("custom:".length());
                soundList.add(serialize(data, stringSound, volume, pitch, delay, noise, distance, mat, category));
                continue;
            }

            try {
                Sound sound = Sound.valueOf(stringSound.toUpperCase(Locale.ROOT));
                soundList.add(serialize(data, sound, volume, pitch, delay, noise, distance, mat, category));
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
        private final double distance;
        private final MaterialCategory headMaterial;
        private final String soundCategory;

        public SoundMechanicData(float volume, float pitch, int delay, float noise, double distance, MaterialCategory headMaterial, String soundCategory) {
            this.volume = Math.min(volume, 1f);
            this.pitch = pitch;
            this.delay = delay;
            this.noise = noise;
            this.distance = Double.isNaN(distance) ? Math.max(1, volume) * 16 : distance;
            this.headMaterial = headMaterial;
            this.soundCategory = soundCategory;
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

        public float getNoise() {
            return noise;
        }

        public MaterialCategory getHeadMaterial() {
            return headMaterial;
        }

        public String getSoundCategory() {
            return soundCategory;
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

        public double getDistance() {
            return distance;
        }

        public Iterable<Entity> getPlayers(CastData cast) {
            Location location = cast.getCastLocation();
            double distance = getDistance();
            return location.getWorld().getNearbyEntities(location, distance, distance, distance, Player.class::isInstance);
        }
    }

    public static class BukkitSound extends SoundMechanicData {

        private final Sound sound;

        public BukkitSound(Sound sound, float volume, float pitch, int delay, float noise, double distance, MaterialCategory headMaterial, String soundCategory) {
            super(volume, pitch, delay, noise, distance, headMaterial, soundCategory);
            this.sound = sound;
        }

        @Override
        public void play(CastData castData) {
            Location location = castData.getCastLocation();

            for (Entity entity : getPlayers(castData)) {
                Player player = (Player) entity;
                if (!getHeadMaterial().test(player))
                    continue;

                if (ReflectionUtil.getMCVersion() >= 11) {
                    SoundCategory category = EnumUtil.getIfPresent(SoundCategory.class, getSoundCategory()).orElse(SoundCategory.PLAYERS);
                    player.playSound(location, sound, category, getVolume(), getRandomPitch());
                } else {
                    player.playSound(location, sound, getVolume(), getRandomPitch());
                }
            }
        }
    }

    public static class CustomSound extends SoundMechanicData {

        private final String sound;

        public CustomSound(String sound, float volume, float pitch, int delay, float noise, double distance, MaterialCategory headMaterial, String soundCategory) {
            super(volume, pitch, delay, noise, distance, headMaterial, soundCategory);
            this.sound = sound;
        }

        @Override
        public void play(CastData castData) {
            Location location = castData.getCastLocation();

            for (Entity entity : getPlayers(castData)) {
                Player player = (Player) entity;
                if (!getHeadMaterial().test(player))
                    continue;

                if (ReflectionUtil.getMCVersion() >= 11) {
                    SoundCategory category = EnumUtil.getIfPresent(SoundCategory.class, getSoundCategory()).orElse(SoundCategory.PLAYERS);
                    player.playSound(location, sound, category, getVolume(), getRandomPitch());
                } else {
                    player.playSound(location, sound, getVolume(), getRandomPitch());
                }
            }
        }
    }

    /**
     * This is used to determine what "kind" of block you are in. Sometimes,
     * we want sounds to have an echo, but only when the player is in an
     * enclosed space. Instead of running calculations every time we play the
     * sound, we simply check if we are in cave_air (Vanilla generates caves
     * using cave_air). This is also good for adventure maps, who can replace
     * normal air with cave_air.
     */
    public enum MaterialCategory {

        ALL {
            @Override
            public boolean test(Block block) {
                return true;
            }
        },
        AIR {
            @Override
            public boolean test(Block block) {
                return !FLUID.test(block) && !CAVE_AIR.test(block) && !VOID_AIR.test(block);
            }
        },
        FLUID {
            @Override
            public boolean test(Block block) {
                if (ReflectionUtil.getMCVersion() < 13) {
                    String name = block.getType().name();
                    return name.endsWith("WATER") || name.endsWith("LAVA");
                } else {
                    if (block.isLiquid())
                        return true;
                    else if (block.getBlockData() instanceof Waterlogged)
                        return ((Waterlogged) block.getBlockData()).isWaterlogged();
                    else
                        return false;
                }
            }
        },
        CAVE_AIR {
            @Override
            public boolean test(Block block) {
                return ReflectionUtil.getMCVersion() >= 13 && block.getType() == Material.CAVE_AIR;
            }
        },
        VOID_AIR {
            @Override
            public boolean test(Block block) {
                return ReflectionUtil.getMCVersion() >= 13 && block.getType() == Material.VOID_AIR;
            }
        };

        public abstract boolean test(Block block);

        public boolean test(Player player) {
            return test(player.getEyeLocation().getBlock());
        }
    }
}
