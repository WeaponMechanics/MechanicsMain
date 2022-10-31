package me.deecaad.core.file.serializers;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerTypeException;
import me.deecaad.core.utils.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Parse a vector from a string. Add ~ for relative input.
 *
 * <ul>
 *     <li>1 1 1</li>
 *     <li>left</li>
 *     <li>down</li>
 *     <li>~1 1 0</li>
 * </ul>
 */
public class VectorSerializer implements Serializer<VectorSerializer> {

    private static final Vector UP = new Vector(0, 1, 0);

    private Direction direction;
    private boolean relative;
    private Vector raw;

    /**
     * Default constructor for serializer.
     */
    public VectorSerializer() {
    }

    public VectorSerializer(Direction direction, boolean relative, Vector raw) {
        this.direction = direction;
        this.relative = relative;
        this.raw = raw;
    }

    public Vector getVector(LivingEntity entity) {
        if (direction != null)
            return direction.getRelative(entity);

        if (raw == null)
            throw new IllegalStateException("Impossible");

        if (relative) {
            Quaternion quaternion = Quaternion.lookAt(entity.getLocation().getDirection(), UP);
            return quaternion.multiply(raw);
        }

        return raw;
    }

    @NotNull
    @Override
    public VectorSerializer serialize(SerializeData data) throws SerializerException {
        String input = data.of().assertExists().get().toString();

        Direction direction = EnumUtil.getIfPresent(Direction.class, input).orElse(null);
        Vector raw = null;
        boolean relative = false;

        if (direction == null) {
            relative = input.startsWith("~");
            if (relative)
                input = input.substring(1);

            String[] split = StringUtil.split(input);
            if (split.length != 3) {
                throw data.exception(null, "Expected 3 numbers in x~x~x format, instead got '" + split.length + "'",
                        SerializerException.forValue(input));
            }

            try {
                double x = Double.parseDouble(split[0]);
                double y = Double.parseDouble(split[1]);
                double z = Double.parseDouble(split[2]);

                raw = new Vector(x, y, z);
            } catch (NumberFormatException ex) {
                throw new SerializerTypeException(this, Number.class, String.class, input, data.of().getLocation());
            }
        }

        return new VectorSerializer(direction, relative, raw);
    }

    public enum Direction {

        UP(Transform::getUp),
        DOWN(t -> t.getUp().multiply(-1.0)),
        RIGHT(Transform::getRight),
        LEFT(t -> t.getRight().multiply(-1.0)),
        FORWARD(Transform::getForward),
        BACKWARD(t -> t.getForward().multiply(-1.0));

        private final Function<Transform, Vector> function;

        Direction(Function<Transform, Vector> function) {
            this.function = function;
        }

        public Vector getRelative(LivingEntity entity) {
            Transform transform = new EntityTransform(entity);
            return function.apply(transform);
        }
    }
}
