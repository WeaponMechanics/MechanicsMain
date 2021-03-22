package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.serializers.ColorSerializer;
import me.deecaad.core.file.serializers.LocationAdjuster;
import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class FireworkMechanic implements IMechanic<FireworkMechanic> {

    private LocationAdjuster locationAdjuster;
    private int flightTime;
    private FireworkEffect fireworkEffect;

    /**
     * Empty constructor to be used as serializer
     */
    public FireworkMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), this);
    }

    public FireworkMechanic(LocationAdjuster locationAdjuster, int flightTime, FireworkEffect fireworkEffect) {
        this.locationAdjuster = locationAdjuster;
        this.flightTime = flightTime;
        this.fireworkEffect = fireworkEffect;
    }

    @Override
    public void use(CastData castData) {
        Location location = locationAdjuster != null ? locationAdjuster.getNewLocation(castData.getCastLocation()) : castData.getCastLocation();

        List<Player> players = new ArrayList<>();

        for (Entity entity : DistanceUtil.getEntitiesInRange(location)) {
            if (entity.getType() != EntityType.PLAYER) {
                continue;
            }
            players.add((Player) entity);
        }

        if (players.isEmpty()) {
            return;
        }
        CompatibilityAPI.getCompatibility().getEntityCompatibility().spawnFirework(location, players, (byte) flightTime, fireworkEffect);
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
                    StringUtil.foundInvalid("firework type"),
                    StringUtil.foundAt(file, path + ".Type", stringFireworkType),
                    StringUtil.debugDidYouMean(stringFireworkType, FireworkEffect.Type.class));
            return null;
        }

        List<Color> colors = convertColorList(file, configurationSection, path + ".Colors");
        if (colors == null) {
            debug.log(LogLevel.ERROR,
                    StringUtil.foundInvalid("firework color list"),
                    StringUtil.foundAt(file, path + ".Colors"),
                    "It either didn't exist or it wasn't properly configured");
            return null;
        }

        List<Color> fadeColors = convertColorList(file, configurationSection, path + ".Fade_Colors");
        boolean flicker = configurationSection.getBoolean(path + ".Flicker", true);
        boolean trail = configurationSection.getBoolean(path + ".Trail", false);

        LocationAdjuster locationAdjuster = new LocationAdjuster().serialize(file, configurationSection, path + ".Location_Adjuster");

        int flightTime = configurationSection.getInt(path + ".Flight_Time", 0);
        if (flightTime < 0) {
            debug.log(LogLevel.ERROR,
                    StringUtil.foundInvalid("firework flight time"),
                    StringUtil.foundAt(file, path + ".Flight_Time", flightTime),
                    "It can't have negative value");
            return null;
        }

        FireworkEffect.Builder fireworkEffectBuilder = FireworkEffect.builder().with(fireworkType).withColor(colors).flicker(flicker).trail(trail);
        if (fadeColors != null) {
            fireworkEffectBuilder.withFade(fadeColors);
        }

        return new FireworkMechanic(locationAdjuster, flightTime, fireworkEffectBuilder.build());
    }

    private List<Color> convertColorList(File file, ConfigurationSection configurationSection, String path) {
        List<String> stringColorList = configurationSection.getStringList(path);
        if (stringColorList == null || stringColorList.isEmpty()) return null;

        ColorSerializer colorSerializer = new ColorSerializer();

        List<Color> colorList = new ArrayList<>();
        for (String stringInList : stringColorList) {
            for (String stringInLine : stringInList.split(", ?")) {

                Color color = colorSerializer.fromString(file, configurationSection, path, stringInLine);

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
