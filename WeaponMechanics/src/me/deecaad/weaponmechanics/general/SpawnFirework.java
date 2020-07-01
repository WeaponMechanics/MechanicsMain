package me.deecaad.weaponmechanics.general;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.ColorSerializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * @deprecated In
 */
public class SpawnFirework implements Serializer<SpawnFirework> {

    private List<FireworkData> fireworkDatas;

    /**
     * Empty constructor to be used as serializer
     */
    public SpawnFirework() { }

    public SpawnFirework(List<FireworkData> fireworkDatas) {
        this.fireworkDatas = fireworkDatas;
    }

    /**
     * @param entity the entity whose location is used to spawn fireworks
     */
    public void spawn(Entity entity) {
        spawn(entity.getLocation());
    }

    /**
     * @param location the lcoation to spawn fireworks
     */
    public void spawn(Location location) {
        for (FireworkData fireworkData : fireworkDatas) {
            location = fireworkData.locationFinder != null ? fireworkData.locationFinder.getNewLocation(location) : location;
            Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.addEffect(fireworkData.fireworkEffect);
            int flightPower = fireworkData.flightPower;
            if (flightPower != 0) {
                fireworkMeta.setPower(flightPower);
            }
            firework.setFireworkMeta(fireworkMeta);
            if (flightPower == 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        firework.detonate();
                    }
                }.runTaskLater(WeaponMechanics.getPlugin(), 2);
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Firework";
    }

    @Override
    public SpawnFirework serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection fireworkSection = configurationSection.getConfigurationSection(path);
        if (fireworkSection == null) {
            return null;
        }
        List<FireworkData> fireworkDatas = new ArrayList<>();
        for (String fireworkName : fireworkSection.getKeys(false)) {
            FireworkData fireworkData = tryFireworkData(file, configurationSection, path + "." + fireworkName);
            if (fireworkData == null) {
                continue;
            }
            fireworkDatas.add(fireworkData);
        }
        if (fireworkDatas.isEmpty()) {
            return null;
        }
        return new SpawnFirework(fireworkDatas);
    }

    public FireworkData tryFireworkData(File file, ConfigurationSection configurationSection, String path) {
        String stringFireworkType = configurationSection.getString(path + ".Firework_Type");
        if (stringFireworkType == null) return null;
        stringFireworkType = stringFireworkType.toUpperCase();

        FireworkEffect.Type fireworkType;
        try {
            fireworkType = FireworkEffect.Type.valueOf(stringFireworkType);
        } catch (IllegalArgumentException e) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid firework type in configurations!",
                    "Located at file " + file + " in " + path + ".Type (" + stringFireworkType + ") in configurations");
            return null;
        }

        List<Color> mainColors = convertColorList(file, configurationSection, path + ".Colors");
        if (mainColors == null) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid firework colors list in configurations!",
                    "Located at file " + file + " in " + path + ".Colors in configurations",
                    "It either didn't exist or it wasn't properly configured");
            return null;
        }

        List<Color> fadeColors = convertColorList(file, configurationSection, path + ".Fade_Colors");
        boolean flicker = configurationSection.getBoolean(path + ".Flicker", true);
        boolean trail = configurationSection.getBoolean(path + ".Trail", false);

        FireworkEffect.Builder fireworkEffectBuilder = FireworkEffect.builder().with(fireworkType).withColor(mainColors).flicker(flicker).trail(trail);
        if (fadeColors != null) {
            fireworkEffectBuilder.withFade(fadeColors);
        }

        LocationFinder locationFinder = new LocationFinder().serialize(file, configurationSection, path + ".Location_Finder");
        int flightPower = configurationSection.getInt(path + ".Flight_Power", 0);
        if (flightPower < 0 || flightPower > 5) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid firework flight power in configurations!",
                    "Located at file " + file + " in " + path + ".Flight_Power (" + flightPower + ") in configurations",
                    "It has to be between 0 and 5!");
            return null;
        }

        FireworkEffect fireworkEffect = fireworkEffectBuilder.build();

        return new FireworkData(locationFinder, flightPower, fireworkEffect);
    }

    public List<Color> convertColorList(File file, ConfigurationSection configurationSection, String path) {
        List<String> stringColors = configurationSection.getStringList(path);
        if (stringColors == null || stringColors.isEmpty()) {
            return null;
        }
        List<Color> colors = new ArrayList<>();
        for (String stringColor : stringColors) {
            stringColor = stringColor.toUpperCase();
            Color color = ColorSerializer.ColorType.fromString(stringColor);
            if (color == null) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid color type in configurations!",
                        "Located at file " + file + " in " + path + " (" + stringColor + ") in configurations");
                return null;
            }
            colors.add(color);
        }
        if (colors.isEmpty()) {
            return null;
        }
        return colors;
    }

    public static class FireworkData {

        private LocationFinder locationFinder;
        private int flightPower;
        private FireworkEffect fireworkEffect;

        public FireworkData(LocationFinder locationFinder, int flightPower, FireworkEffect fireworkEffect) {
            this.locationFinder = locationFinder;
            this.flightPower = flightPower;
            this.fireworkEffect = fireworkEffect;
        }

        /**
         * @return the location finder for firework
         */
        public LocationFinder getLocationFinder() {
            return locationFinder;
        }

        /**
         * @return the flight power of firework
         */
        public int getFlightPower() {
            return flightPower;
        }

        /**
         * @return the effect of firework
         */
        public FireworkEffect getFireworkEffect() {
            return fireworkEffect;
        }
    }
}