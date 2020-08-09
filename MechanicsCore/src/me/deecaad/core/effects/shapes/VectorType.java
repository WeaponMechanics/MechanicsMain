package me.deecaad.core.effects.shapes;

import me.deecaad.core.mechanics.serialization.SerializerData;
import me.deecaad.core.mechanics.serialization.StringSerializable;
import me.deecaad.core.utils.VectorUtils;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Map;

@SerializerData(name = "vector", args = "length~DOUBLE")
public abstract class VectorType implements StringSerializable<VectorType> {

    protected double length;

    protected VectorType(double length) {
        this.length = length;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public boolean hasLength() {
        return length > 0;
    }

    @Override
    public VectorType serialize(Map<String, Object> data) {
        return null;
    }

    @Nullable
    public abstract Vector getVector();

    /**
     * Defines a directional vector with a defined x, y, and z component
     */
    @SerializerData(name = "directional", args = {"x~DOUBLE", "y~DOUBLE", "z~DOUBLE"})
    public static final class Directional extends VectorType {

        public Vector vector;

        /**
         * Default constructor for serializer
         */
        public Directional() {
            this(0, 0 ,0 ,-1);
        }

        public Directional(double x, double y, double z, double length) {
            super(length);

            this.vector = new Vector(x, y, z);
            if (hasLength()) {
                VectorUtils.setLength(vector, length);
            }
        }

        @Override
        public Vector getVector() {
            return vector;
        }

        @Override
        public VectorType serialize(Map<String, Object> data) {
            super.serialize(data);

            double x = (double) data.getOrDefault("x", 0);
            double y = (double) data.getOrDefault("y", 0);
            double z = (double) data.getOrDefault("z", 0);

            this.vector = new Vector(x, y, z);
            if (hasLength()) {
                VectorUtils.setLength(vector, length);
            }

            return this;
        }
    }

    /**
     * Defines a random vector, with a randomized x, y, and z component
     */
    @SerializerData(name = "random")
    public static final class Random extends VectorType {

        /**
         * Default constructor for serializer
         */
        public Random() {
            super(1);
        }

        public Random(double length) {
            super(length);
        }

        @Override
        public Vector getVector() {
            return VectorUtils.random(length);
        }

        @Override
        public VectorType serialize(Map<String, Object> data) {
            super.serialize(data);
            return this;
        }
    }

    /**
     * Defines an unknown vector. It is up to the program to handle the "guess"
     * part of this. Generally this should be the entity's yaw and pitch.
     */
    @SerializerData(name = "guess")
    public static final class Guess extends VectorType {

        /**
         * Default constructor for serializer
         */
        public Guess() {
            this(-1);
        }

        public Guess(double length) {
            super(length);
        }

        @Override
        public Vector getVector() {
            return null;
        }

        @Override
        public VectorType serialize(Map<String, Object> data) {
            super.serialize(data);
            return this;
        }
    }

}
