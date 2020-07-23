package me.deecaad.core.mechanics.serialization.datatypes;

import org.bukkit.entity.EntityType;

import java.util.LinkedHashMap;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

/**
 * This class is not thread safe, possible deadlock
 * @param <T>
 */
@SuppressWarnings({"StaticInitializerReferencesSubClass", "unchecked"})
public abstract class DataType<T> {

    public static final DataType<Integer> INTEGER = new IntegerType();
    public static final DataType<Double> DOUBLE = new DoubleType();
    public static final DataType<Boolean> BOOLEAN = new BooleanType();
    public static final DataType<EntityType> ENTITY = new EnumType(EntityType.class);
    public static final DataType<String> STRING = new StringType();

    private static final Map<String, DataType<?>> BY_NAME = new LinkedHashMap<>();

    private final String name;

    public DataType(String name) {
        this.name = name;

        DataType<?> previous = DataType.BY_NAME.put(name, this);
        if (previous != null) {
            debug.warn("A DataType \"" + name + "\" was overridden");
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
}
