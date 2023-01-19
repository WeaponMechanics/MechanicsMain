package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerTypeException;
import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.serializers.VectorSerializer;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.util.Vector;

public class VectorType implements ArgumentType<VectorSerializer> {

    public VectorType() {
    }

    @Override
    public VectorSerializer serialize(String input) throws InlineException {

        // Allow for easy 0 option
        if ("0".equals(input))
            return VectorSerializer.from(new Vector());

        double randomLength = -1.0;
        VectorSerializer.Direction direction = EnumUtil.getIfPresent(VectorSerializer.Direction.class, input).orElse(null);
        Vector raw = null;
        boolean relative = false;

        if (direction == null) {

            // When input starts with an 'r', then the input is random.
            if (input.startsWith("r")) {
                try {
                    randomLength = Double.parseDouble(input.substring(1));
                } catch (NumberFormatException ex) {
                    throw new InlineException(input, new SerializerTypeException("", Number.class, String.class, input, ""));
                }
            }

            // When input starts with a '~', then the input is relative.
            // This means that instead of x-y-z, it is left-up-forward.
            relative = input.startsWith("~");
            if (relative)
                input = input.substring(1);

            String[] split = StringUtil.split(input);
            if (split.length != 3) {
                throw new InlineException(input, new SerializerException("", new String[] {"Expected 3 numbers in left~up~forward format, instead got '" + split.length + "'",
                        SerializerException.forValue(input)}, ""));
            }

            try {
                double x = Double.parseDouble(split[0]);
                double y = Double.parseDouble(split[1]);
                double z = Double.parseDouble(split[2]);

                raw = new Vector(x, y, z);
            } catch (NumberFormatException ex) {
                throw new InlineException(input, new SerializerTypeException("", Number.class, String.class, input, ""));
            }
        }

        return new VectorSerializer(randomLength, direction, relative, raw);
    }

    @Override
    public String example() {
        return "0 1 0";
    }
}
