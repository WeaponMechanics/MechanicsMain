package me.deecaad.core.file.serializers;

import me.deecaad.core.file.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

public class ChanceSerializer implements SimpleSerializer<Double> {

    /**
     * Default constructor for serializer.
     */
    public ChanceSerializer() {
    }

    @Override
    public @NotNull String getTypeName() {
        return "chance";
    }

    @Override
    public @NotNull Double deserialize(@NotNull String value, @NotNull String errorLocation) throws SerializerException {
        double chance;

        // Handle percentages. This is the officially supported method of
        // parsing a chance. Users may use decimals when using percentages.
        // For example, 10.5% = 0.105
        if (value.contains("%")) {
            String str = value.trim();

            // We should account for %50 and 50%, since it can be easy to mix
            // those up.
            if (str.startsWith("%")) {
                chance = Double.parseDouble(str.substring(1)) / 100.0;
            } else if (str.endsWith("%")) {
                chance = Double.parseDouble(str.substring(0, str.length() - 1)) / 100.0;
            } else {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Expected a percentage")
                    .example("50%")
                    .buildInvalidType("Chance", value);
            }
        }

        // Consider all numbers to be valid
        else {
            try {
                chance = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Expected a number or percentage")
                    .example("50%")
                    .buildInvalidType("Chance", value);
            }
        }

        if (chance < 0.0 || chance > 1.0) {
            throw SerializerException.builder()
                .locationRaw(errorLocation)
                .addMessage("When using percentages, make sure to stay between 0% and 100%")
                .buildInvalidRange(chance, 0.0, 1.0);
        }

        return chance;
    }

    @Override
    public @NotNull List<String> examples() {
        return IntStream.rangeClosed(0, 100).mapToObj(i -> i + "%").toList();
    }
}
