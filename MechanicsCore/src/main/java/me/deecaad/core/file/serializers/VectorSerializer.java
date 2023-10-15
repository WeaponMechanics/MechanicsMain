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
 *     <li>0</li>
 *     <li>r1.0</li>
 *     <li>1 1 1</li>
 *     <li>left</li>
 *     <li>down</li>
 *     <li>~1 1 0</li>
 * </ul>
 */
public class VectorSerializer implements Serializer<VectorSerializer> {

    private static final Vector UP = new Vector(0, 1, 0);

    private double randomLength;
    private Direction direction;
    private boolean relative;
    private Vector raw;

    /**
     * Default constructor for serializer.
     */
    public VectorSerializer() {
    }

    public VectorSerializer(double randomLength, Direction direction, boolean relative, Vector raw) {
        this.randomLength = randomLength;
        this.direction = direction;
        this.relative = relative;
        this.raw = raw;
    }

    public boolean isRelative() {
        return relative;
    }

    /**
     * Returns the vector using the given entity to determine relative
     * directions. If the given entity is null, relativity is ignored.
     *
     * @param entity The nullable entity.
     * @return The non-null vector.
     */
    public Vector getVector(LivingEntity entity) {
        if (randomLength >= 0)
            return VectorUtil.random(randomLength);

        if (direction != null)
            return direction.getRelative(entity == null ? null : entity.getLocation().getDirection());

        if (raw == null)
            throw new IllegalStateException("Impossible");

        if (this.relative && entity != null) {
            return new EntityTransform(entity).getLocalRotation().multiply(raw);
        }

        return raw;
    }

    /**
     * Returns the vector using the given vector to determine relative
     * directions. If the given entity is null, relativity is ignored.
     *
     * @param view The nullable relative direction.
     * @return The non-null vector.
     */
    public Vector getVector(Vector view) {
        if (randomLength >= 0)
            return VectorUtil.random(randomLength);

        if (direction != null)
            return direction.getRelative(view);

        if (raw == null)
            throw new IllegalStateException("Impossible");

        if (this.relative && view != null) {
            Vector localUp = UP.equals(view) ? new Vector(-1, 0, 0) : UP;
            Quaternion quaternion = Quaternion.lookAt(view, localUp);
            return quaternion.multiply(raw);
        }

        return raw;
    }

    @NotNull
    @Override
    public VectorSerializer serialize(@NotNull SerializeData data) throws SerializerException {
        String input = data.of().assertExists().get().toString().trim();

        // Allow for easy 0 option
        if ("0".equals(input))
            return VectorSerializer.from(new Vector());

        double randomLength = -1.0;
        Direction direction = EnumUtil.getIfPresent(Direction.class, input).orElse(null);
        Vector raw = null;
        boolean relative = false;

        if (direction == null) {

            // When input starts with an 'r', then the input is random.
            if (input.startsWith("r")) {
                try {
                    randomLength = Double.parseDouble(input.substring(1));
                } catch (NumberFormatException ex) {
                    throw new SerializerTypeException(this, Number.class, String.class, input, data.of().getLocation());
                }
            }

            // When input starts with a '~', then the input is relative.
            // This means that instead of x-y-z, it is left-up-forward.
            relative = input.startsWith("~");
            if (relative)
                input = input.substring(1);

            String[] split = StringUtil.split(input);
            if (split.length != 3) {
                throw data.exception(null, "Expected 3 numbers in left~up~forward format, instead got '" + split.length + "'",
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

        return new VectorSerializer(randomLength, direction, relative, raw);
    }


    /**
     * Helper method to always return a raw vector from a vector serializer.
     * This is helpful for zeroing input.
     *
     * @param vector The non-null vector.
     * @return The wrapped vector.
     */
    public static VectorSerializer from(Vector vector) {
        return new VectorSerializer() {
            @Override
            public Vector getVector(LivingEntity entity) {
                return vector;
            }

            @Override
            public Vector getVector(Vector relative) {
                return vector;
            }
        };
    }

    /**
     * Enum holding the basic cardinal directions for simple user input.
     */
    public enum Direction {

        UP(Transform::getUp, 0, 1, 0),
        DOWN(t -> t.getUp().multiply(-1.0), 0, -1, 0),
        RIGHT(Transform::getRight, 1, 0, 0),
        LEFT(t -> t.getRight().multiply(-1.0), -1, 0, 0),
        FORWARD(Transform::getForward, 0, 0, 1),
        BACKWARD(t -> t.getForward().multiply(-1.0), 0, 0, -1);

        private final Function<Transform, Vector> function;
        private final Vector raw;

        Direction(Function<Transform, Vector> function, double x, double y, double z) {
            this.function = function;
            this.raw = new Vector(x, y, z);
        }

        public Vector getRelative(LivingEntity livingEntity) {
            if (livingEntity == null)
                return raw.clone();

            Transform transform = new EntityTransform(livingEntity);
            return function.apply(transform);
        }

        public Vector getRelative(Vector relative) {
            if (relative == null)
                return raw.clone();

            Transform transform = new Transform();
            transform.setLocalRotation(Quaternion.lookAt(relative, VectorSerializer.UP));
            return function.apply(transform);
        }
    }
}
