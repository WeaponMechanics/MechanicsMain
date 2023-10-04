package me.deecaad.core.file.serializers;

import me.deecaad.core.file.*;
import org.jetbrains.annotations.NotNull;

public class ChanceSerializer implements Serializer<Double> {

    /**
     * Default constructor for serializer.
     */
    public ChanceSerializer() {
    }

    @NotNull
    @Override
    public Double serialize(@NotNull SerializeData data) throws SerializerException {
        Object value = data.of().assertExists().get();
        double chance = 0.0;

        // Handle percentages. This is the officially supported method of
        // parsing a chance. Users may use decimals when using percentages.
        // For example, 10.5% = 0.105
        if (value.toString().contains("%")) {
            String str = value.toString().trim();

            // We should account for %50 and 50%, since it can be easy to mix
            // those up.
            if (str.startsWith("%")) {
                chance = Double.parseDouble(str.substring(1)) / 100.0;
            } else if (str.endsWith("%")) {
                chance = Double.parseDouble(str.substring(0, str.length() - 1)) / 100.0;
            } else {
                throw data.exception(null, "Chance input had a '%' in the middle when it should have been on the end",
                        SerializerException.forValue(value));
            }
        }

        // Consider all numbers to be valid
        else if (value instanceof Number) {
            chance = ((Number) value).doubleValue();
        }

        // After checking for numbers, and percentages, there is nothing else
        // we can do except yell at the user for being stupid.
        else {
            throw new SerializerTypeException(data.serializer, Number.class, value.getClass(), value, data.of().getLocation());
        }


        if (chance < 0.0 || chance > 1.0) {
            throw new SerializerRangeException(data.serializer, 0.0, chance, 1.0, data.of().getLocation())
                    .addMessage("When using percentages, make sure to stay between 0% and 100%");
        }

        return chance;
    }
}
