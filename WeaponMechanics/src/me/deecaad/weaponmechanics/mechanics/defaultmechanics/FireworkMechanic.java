package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.LocationAdjuster;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class FireworkMechanic implements Serializer<FireworkMechanic>, IMechanic {

    private LocationAdjuster locationAdjuster;
    private int flightPower;
    private FireworkEffect fireworkEffect;

    /**
     * Empty constructor to be used as serializer
     */
    public FireworkMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), getKeyword());
    }

    public FireworkMechanic(LocationAdjuster locationAdjuster, int flightPower, FireworkEffect fireworkEffect) {
        this.locationAdjuster = locationAdjuster;
        this.flightPower = flightPower;
        this.fireworkEffect = fireworkEffect;
    }

    @Override
    public void use(CastData castData) {
        Location location = castData.getCastLocation();
        if (locationAdjuster != null) location = locationAdjuster.getNewLocation(location);

        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(fireworkEffect);
        if (flightPower != 0) {
            fireworkMeta.setPower(flightPower);
        } else {

            // Almost instant spawn.
            // If wanted to make instant, requires some NMS.
            new BukkitRunnable() {
                @Override
                public void run() {
                    firework.detonate();
                }
            }.runTaskLater(WeaponMechanics.getPlugin(), 2);
        }
        firework.setFireworkMeta(fireworkMeta);
    }

    @Override
    public String getKeyword() {
        return "Firework";
    }

    @Override
    public FireworkMechanic serialize(File file, ConfigurationSection configurationSection, String path) {
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

        List<Color> colors = convertColorList(file, configurationSection, path + ".Colors");
        if (colors == null) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid firework colors list in configurations!",
                    "Located at file " + file + " in " + path + ".Colors in configurations",
                    "It either didn't exist or it wasn't properly configured");
            return null;
        }

        List<Color> fadeColors = convertColorList(file, configurationSection, path + ".Fade_Colors");
        boolean flicker = configurationSection.getBoolean(path + ".Flicker", true);
        boolean trail = configurationSection.getBoolean(path + ".Trail", false);

        LocationAdjuster locationAdjuster = new LocationAdjuster().serialize(file, configurationSection, path + ".Location_Adjuster");

        int flightPower = configurationSection.getInt(path + ".Flight_Power", 0);
        if (flightPower < 0 || flightPower > 5) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid firework flight power in configurations!",
                    "Located at file " + file + " in " + path + ".Flight_Power (" + flightPower + ") in configurations",
                    "It has to be between 0 and 5!");
            return null;
        }

        FireworkEffect.Builder fireworkEffectBuilder = FireworkEffect.builder().with(fireworkType).withColor(colors).flicker(flicker).trail(trail);
        if (fadeColors != null) {
            fireworkEffectBuilder.withFade(fadeColors);
        }

        return new FireworkMechanic(locationAdjuster, flightPower, fireworkEffectBuilder.build());
    }

    private List<Color> convertColorList(File file, ConfigurationSection configurationSection, String path) {
        List<String> stringColorList = configurationSection.getStringList(path);
        if (stringColorList == null || stringColorList.isEmpty()) return null;

        List<Color> colorList = new ArrayList<>();
        for (String stringInList : stringColorList) {
            for (String stringInLine : stringInList.split(", ?")) {

                Color color = null; // todo add support for ColorSerializer and parse stringInLine

                if (color == null) {
                    continue;
                }
                colorList.add(color);
            }
        }
        if (colorList.isEmpty()) return null;
        return colorList;
    }
}
