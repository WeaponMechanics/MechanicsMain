package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ListHolder<T extends Enum<T>> {

    private boolean allowAny;
    private boolean whitelist;
    private double defaultSpeedMultiplier;
    private Map<T, Double> list;

    public ListHolder(boolean allowAny, boolean whitelist, double defaultSpeedMultiplier, Map<T, Double> list) {
        this.allowAny = allowAny;
        this.whitelist = whitelist;
        this.defaultSpeedMultiplier = defaultSpeedMultiplier;
        this.list = list;
    }

    public ListHolder() { }

    /**
     * If this is null, that means key is NOT valid
     *
     * @param key the key
     * @return the speed modifier of key or null if it's not valid
     */
    @Nullable
    public Double isValid(T key) {
        if (allowAny) {
            // Since all values are valid, simply return speed modifier
            if (list == null) return defaultSpeedMultiplier;

            // The value of key might be null if it doesn't have value defined
            Double value = list.getOrDefault(key, defaultSpeedMultiplier);
            return value == null ? defaultSpeedMultiplier : value;
        }

        if (!whitelist) {
            // If blacklist and list contains key
            // -> Can't use
            // Else return speed modifier

            // Speed modifier wont work with blacklist
            return list.containsKey(key) ? null : defaultSpeedMultiplier;
        }

        // If whitelist and list DOES NOT contain key
        // -> Can't use
        // Else return speed modifier
        if (!list.containsKey(key)) return null;

        // The value of key might be null if it doesn't have value defined
        Double value = list.getOrDefault(key, defaultSpeedMultiplier);
        return value == null ? defaultSpeedMultiplier : value;
    }

    public ListHolder<T> serialize(File file, ConfigurationSection configurationSection, String path, Class<T> clazz) {
        boolean allowAny = configurationSection.getBoolean(path + ".Allow_Any", false);

        Map<T, Double> mapList = new HashMap<>();
        List<?> list = configurationSection.getList(path + ".List");
        if (list == null || list.isEmpty()) {
            if (!allowAny) {
                return null;
            }
        } else {
            for (Object data : list) {
                String[] split = StringUtil.split(data.toString());
                Double speedMultiplier = null;

                // Make optional to use speed multiplier
                if (split.length >= 2) {
                    try {
                        speedMultiplier = Double.parseDouble(split[1]);
                    } catch (NumberFormatException e) {
                        MechanicsCore.debug.log(LogLevel.ERROR,
                                StringUtil.foundInvalid("value"),
                                StringUtil.foundAt(file, path + ".List", data.toString()),
                                "Tried to get get number from " + split[1] + ", but it wasn't a number?");
                        continue;
                    }
                }

                List<T> validValues = EnumUtil.parseEnums(clazz, split[0]);
                if (validValues.isEmpty()) {
                    debug.log(LogLevel.ERROR,
                            StringUtil.foundInvalid(clazz.getSimpleName()),
                            StringUtil.foundAt(file, path + ".List", data.toString()),
                            StringUtil.debugDidYouMean(split[0].toUpperCase(), clazz));
                    continue;
                }

                for (T validValue : validValues) {
                    // Speed multiplier is null if it isn't defined
                    mapList.put(validValue, speedMultiplier);
                }
            }
        }

        if (mapList.isEmpty()) {
            if (!allowAny) {
                return null;
            }
            mapList = null;
        }

        double defaultSpeedMultiplier = configurationSection.getDouble(path + ".Default_Speed_Multiplier", 1.0);
        boolean whitelist = configurationSection.getBoolean(path + ".Whitelist", true);
        return new ListHolder<>(allowAny, whitelist, defaultSpeedMultiplier, mapList);
    }
}
