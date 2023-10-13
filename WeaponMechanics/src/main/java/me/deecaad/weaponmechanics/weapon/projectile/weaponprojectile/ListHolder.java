package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.EnumUtil;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListHolder<T extends Enum<T>> implements Serializer<ListHolder<T>> {

    private Class<T> clazz;

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

    public ListHolder(Class<T> clazz) {
        this.clazz = clazz;
    }

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

    @Override
    @NotNull
    public ListHolder<T> serialize(@NotNull SerializeData data) throws SerializerException {
        boolean allowAny = data.of("Allow_Any").getBool(false);

        Map<T, Double> mapList = new HashMap<>();
        List<String[]> list = data.ofList("List")
                .addArgument(clazz, true)
                .addArgument(double.class, false)
                .assertList().get();

        for (String[] split : list) {
            Double speedMultiplier = null;

            // Make optional to use speed multiplier
            if (split.length >= 2) {
                speedMultiplier = Double.parseDouble(split[1]);
            }

            List<T> validValues = EnumUtil.parseEnums(clazz, split[0]);

            for (T validValue : validValues) {
                // Speed multiplier is null if it isn't defined
                mapList.put(validValue, speedMultiplier);
            }
        }

        if (mapList.isEmpty()) {
            if (!allowAny) {
                throw data.exception(null, "'List' found without any valid options",
                        "This happens when 'Allow_Any: false' and 'List' is empty");
            }
            mapList = null;
        }

        double defaultSpeedMultiplier = data.of("Default_Speed_Multiplier").assertPositive().getDouble(1.0);
        boolean whitelist = data.of("Whitelist").getBool(true);
        return new ListHolder<>(allowAny, whitelist, defaultSpeedMultiplier, mapList);
    }
}
