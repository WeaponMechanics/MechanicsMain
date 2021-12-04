package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class ProjectileListData<T extends Enum<T>> {

    private boolean allowAny;
    private boolean whitelist;
    private double defaultSpeedModifier;

    /**
     * T = Material or EntityType
     * Double = Speed modifier
     */
    private Map<T, Double> list;

    public ProjectileListData() { }

    public ProjectileListData(boolean allowAny, boolean whitelist, double defaultSpeedModifier, Map<T, Double> list) {
        this.allowAny = allowAny;
        this.whitelist = whitelist;
        this.defaultSpeedModifier = defaultSpeedModifier;
        this.list = list;
    }

    /**
     * If this is null, that means key is NOT valid
     *
     * @param key the key
     * @return the speed modifier of key or null if its not valid
     */
    @Nullable
    public Double isValid(T key) {
        if (allowAny) {
            // Since all values are valid, simply return speed modifier
            return list == null ? defaultSpeedModifier : list.getOrDefault(key, defaultSpeedModifier);
        }

        if (!whitelist) {
            // If blacklist and list contains key
            // -> Can't use
            // Else return speed modifier
            return list.containsKey(key) ? null : list.getOrDefault(key, defaultSpeedModifier);
        }
        // If whitelist and list DOES NOT contain key
        // -> Can't use
        // Else return speed modifier
        return !list.containsKey(key) ? null : list.getOrDefault(key, defaultSpeedModifier);
    }

    /**
     * This is not registered serializer, its only meant to parse projectile block and entity lists
     *
     * @param clazz the enum class
     * @param file the file being filled
     * @param configurationSection the configuration section
     * @param path the path to this serializer's path (path to keyword like path.keyword)
     * @return the serialized object or null
     */
    public ProjectileListData<T> serialize(Class<T> clazz, File file, ConfigurationSection configurationSection, String path) {

        boolean allowAny = clazz.equals(Material.class) ?
                configurationSection.getBoolean(path + ".Allow_Any_Block", false) :
                configurationSection.getBoolean(path + ".Allow_Any_Entity", false);

        // Double since its nullable
        Map<T, Double> mapList = new HashMap<>();

        List<?> list = configurationSection.getList(path + ".List");
        if (list == null || list.isEmpty()) {
            if (!allowAny) {
                return null;
            }
        } else {
            for (Object data : list) {
                String[] split = StringUtil.split(data.toString());

                Double speedModifier = null;
                if (split.length >= 2) {
                    try {
                        speedModifier = Double.parseDouble(split[1]);
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
                    mapList.put(validValue, speedModifier);
                }
            }
        }

        if (mapList.isEmpty()) {
            if (!allowAny) {
                return null;
            }
            mapList = null;
        }

        double defaultSpeedModifier = configurationSection.getDouble(path + ".Default_Speed_Modifier", 1.0);
        boolean whitelist = configurationSection.getBoolean(path + ".Whitelist", true);
        return new ProjectileListData<>(allowAny, whitelist, defaultSpeedModifier, mapList);
    }
}