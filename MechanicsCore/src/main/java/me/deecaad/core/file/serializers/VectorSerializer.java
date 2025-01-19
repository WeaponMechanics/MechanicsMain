package me.deecaad.core.file.serializers;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.ImmutableVector;
import me.deecaad.core.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Parse a vector from a string. Add ~ for relative input.
 *
 * <ul>
 * <li>0</li>
 * <li>r1.0</li>
 * <li>1 1 1</li>
 * <li>left</li>
 * <li>down</li>
 * <li>~1 1 0</li>
 * </ul>
 */
public class VectorSerializer implements Serializer<VectorProvider> {

    /**
     * Default constructor for serializer.
     */
    public VectorSerializer() {
    }

    @Override
    public @NotNull VectorProvider serialize(@NotNull SerializeData data) throws SerializerException {
        String input = data.of().assertExists().get(Object.class).get().toString().trim();

        // Allow for easy 0 option
        if ("0".equals(input)) {
            return new AnyVectorProvider(false, new ImmutableVector(0, 0, 0));
        }

        // For a more human-readable input
        Optional<Direction> direction = EnumUtil.getIfPresent(Direction.class, input);
        if (direction.isPresent()) {
            return direction.get();
        }

        // Random vector with set length
        if (input.startsWith("r")) {
            double randomLength;
            try {
                randomLength = Double.parseDouble(input.substring(1));
            } catch (NumberFormatException ex) {
                throw SerializerException.builder()
                    .locationRaw(data.of().getLocation())
                    .addMessage("Expected a number after 'r' for random length")
                    .addMessage("Found value: " + input)
                    .build();
            }

            return new RandomVectorProvider(randomLength, randomLength);
        }

        // When input starts with a '~', then the input is relative.
        // This means that instead of x-y-z, it is left-up-forward.
        boolean relative = input.startsWith("~");
        if (relative)
            input = input.substring(1);

        List<String> split = StringUtil.split(input);
        if (split.size() != 3) {
            throw data.exception(null, "Expected 3 numbers in left~up~forward format, instead got '" + split.size() + "'",
                "Found value: " + input);
        }

        try {
            double x = Double.parseDouble(split.get(0));
            double y = Double.parseDouble(split.get(1));
            double z = Double.parseDouble(split.get(2));

            return new AnyVectorProvider(relative, new ImmutableVector(x, y, z));
        } catch (NumberFormatException ex) {
            throw SerializerException.builder()
                .locationRaw(data.of().getLocation())
                .addMessage("Expected 3 numbers in left~up~forward format")
                .addMessage("Found value: " + input)
                .build();
        }
    }
}
