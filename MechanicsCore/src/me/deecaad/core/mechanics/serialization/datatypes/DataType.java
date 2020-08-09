package me.deecaad.core.mechanics.serialization.datatypes;

import me.deecaad.core.effects.shapes.VectorType;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.entity.EntityType;

import java.util.LinkedHashMap;

import static me.deecaad.core.MechanicsCore.debug;

/**
 * This class is not thread safe, possible deadlock
 * @param <T> The type to serialize
 */
@SuppressWarnings({"StaticInitializerReferencesSubClass", "unchecked"})
public abstract class DataType<T> {

    private static final LinkedHashMap<String, DataType<?>> BY_NAME = new LinkedHashMap<>();

    public static final DataType<Integer> INTEGER = new IntegerType();
    public static final DataType<Double> DOUBLE = new DoubleType();
    public static final DataType<Boolean> BOOLEAN = new BooleanType();
    public static final DataType<EntityType> ENTITY = new EnumType(EntityType.class, "ENTITY");
    public static final DataType<VectorType> VECTOR = new SerializerType<>(VectorType.class, "VECTOR");
    public static final DataType<String> STRING = new StringType();

    private final String name;

    public DataType(String name) {
        this.name = name.trim().toUpperCase();

        DataType<?> previous = DataType.BY_NAME.put(this.name, this);
        if (previous != null) {
            debug.warn("A DataType \"" + this.name + "\" was overridden");
        }
    }

    public String getName() {
        return name;
    }

    /**
     *
     * @param str
     * @return
     */
    public abstract T serialize(String str);

    /**
     *
     * @param str
     * @return
     */
    public abstract boolean validate(String str);

    public static Object getValue(String str) {
        for (DataType<?> type : BY_NAME.values()) {
            if (type.validate(str)) {
                return type.serialize(str);
            }
        }

        // String doesn't match any type, so defaults to string value.
        return str;
    }

    public static DataType<?> valueOf(String str) {
        return BY_NAME.get(str.trim().toUpperCase());
    }

    @Override
    public String toString() {
        return StringUtils.keyToRead(name);
    }
}
