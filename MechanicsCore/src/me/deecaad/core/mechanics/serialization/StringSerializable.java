package me.deecaad.core.mechanics.serialization;

import me.deecaad.core.mechanics.serialization.datatypes.DataType;
import me.deecaad.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

public interface StringSerializable<T> {

    Map<Class<?>, Argument[]> CACHE = new HashMap<>();

    T serialize(Map<String, Object> data);

    static Argument[] parseArgs(Class<?> clazz) {

        // Check if data on this class was saved in cache
        if (CACHE.containsKey(clazz)) {
            return CACHE.get(clazz);
        }

        // Class has no arguments defined. This is an error
        if (!clazz.isAnnotationPresent(SerializerData.class)) {
            debug.warn("Class " + clazz + " failed to provide data for SerializerData!",
                    "Any class that implements StringSerializable MUST be annotated by SerializerData");

            return new Argument[0];
        }

        String[] strings = clazz.getAnnotation(SerializerData.class).args();
        List<Argument> args = new ArrayList<>(strings.length);

        // Parse data from the annotation
        for (int i = 0; i < strings.length; i++) {
            String[] split = StringUtils.split(strings[i]);
            String name = split[0];
            DataType<?> type = DataType.valueOf(split[1]);
            String[] aliases = split.length > 2 ? split[2].split(",") : new String[0];

            if (type == null) {
                throw new IllegalArgumentException("HEY, DEVELOPER!!! Unknown type: " + split[1]);
            }
            
            args.add(new Argument(name, type, aliases));
        }

        // Recursively check superclasses for arguments
        if (clazz.getSuperclass() != Object.class && clazz.getSuperclass().isAssignableFrom(StringSerializable.class)) {
            List<Argument> superArgs = Arrays.asList(parseArgs(clazz.getSuperclass()));
            args.addAll(superArgs);
        }

        debug.debug("Class " + clazz.getSimpleName() + " has args: " + args);
        Argument[] arr = args.toArray(new Argument[0]);
        CACHE.put(clazz, arr);
        return arr;
    }

    static String parseName(Class<? extends StringSerializable> clazz) {

        // Class has no arguments defined. This is an error
        if (!clazz.isAnnotationPresent(SerializerData.class)) {
            debug.warn("Class " + clazz + " failed to provide data for SerializerData!",
                    "Any class that implements StringSerializable MUST be annotated by SerializerData");

            return "";
        }

        return clazz.getAnnotation(SerializerData.class).name();
    }
}
