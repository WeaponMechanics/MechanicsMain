package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.utils.ReflectionUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Factory<T> {

    private final Map<String, Arguments> map;

    /**
     * Only subclasses should be able to instantiate this class. It does not
     * make sense to instantiate a factory otherwise.
     */
    protected Factory(int initialSize) {
        map = new HashMap<>(initialSize);
    }

    public final T get(String key, Map<String, Object> arguments) {
        key = key.trim().toUpperCase(Locale.ROOT);
        Arguments args = map.get(key);

        if (args == null)
            throw new IllegalArgumentException("Unexpected input: " + key);

        // Pull only the values that we need from the mapped arguments. The
        // order of the arguments will match the order defined by the
        // Arguments class.
        Object[] objects = new Object[args.arguments.length];
        for (int i = 0; i < args.arguments.length; i++) {
            String argument = args.arguments[i];

            if (!arguments.containsKey(argument))
                throw new IllegalArgumentException("Map does not contain data: " + argument);

            objects[i] = arguments.get(argument);
        }

        return ReflectionUtil.newInstance(args.manufacturedType, objects);
    }

    @SuppressWarnings("unchecked")
    public final void set(String key, Class<? extends T> manufacturedType, String... arguments) {
        Arguments args = new Arguments((Class<T>) manufacturedType, arguments);
        map.put(key.trim().toUpperCase(Locale.ROOT), args);
    }

    public Set<String> getOptions() {
        return map.keySet();
    }

    private class Arguments {
        private final Class<T> manufacturedType;
        private final String[] arguments;

        public Arguments(Class<T> manufacturedType, String[] arguments) {
            this.manufacturedType = manufacturedType;
            this.arguments = arguments;
        }
    }
}
