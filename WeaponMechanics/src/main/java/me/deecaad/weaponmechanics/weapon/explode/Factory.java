package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;

import java.lang.reflect.Field;
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
    protected Factory() {
        map = new HashMap<>();
    }

    /**
     * Returns a constructed object of who inherits from <code>T</code>,
     * constructed from the given <code>arguments</code>. In order to add a
     * new type, use {@link #set(String, Arguments)}.
     *
     * <p>The given <code>arguments</code> <i>MUST</i> explicitly contain
     * <i>ALL</i> objects defined by the {@link Arguments#arguments}. If an
     * argument is missing, a {@link FactoryException} is thrown. The given
     * objects are type-casted to their expected type. Ensure that the
     * constructors for your defined arguments exist.
     *
     * @param key The non-null, non-case-sensitive name of the class to instantiate.
     * @param arguments The non-null map of arguments.
     * @return Instantiated object.
     * @throws InternalError If no "good" constructor exists.
     * @throws FactoryException If there were missing arguments.
     * @throws ClassCastException If a given argument is an invalid type.
     */
    public final T get(String key, Map<String, Object> arguments) {
        key = key.trim().toUpperCase(Locale.ROOT);
        Arguments args = map.get(key);

        if (args == null)
            return null;

        // Pull only the values that we need from the mapped arguments. The
        // order of the arguments will match the order defined by the
        // Arguments class.
        Object[] objects = new Object[args.arguments.length];
        for (int i = 0; i < args.arguments.length; i++) {
            String argument = args.arguments[i];
            Class<?> clazz = args.argumentTypes[i];

            if (!arguments.containsKey(argument))
                throw new FactoryException(this, key, argument, arguments);

            objects[i] = clazz == null
                    ? arguments.get(argument)
                    : clazz.cast(arguments.get(argument));
        }

        return ReflectionUtil.newInstance(args.manufacturedType, objects);
    }

    public final void set(String key, Arguments args) {
        for (String str : key.split(", ?")) {
            map.put(str.trim().toUpperCase(Locale.ROOT), args);
        }
    }

    public Set<String> getOptions() {
        return map.keySet();
    }


    public class Arguments {

        private final Class<T> manufacturedType;
        private final String[] arguments;
        private final Class<?>[] argumentTypes;

        @SuppressWarnings("unchecked")
        public Arguments(Class<? extends T> manufacturedType, String[] arguments) {
            this.manufacturedType = (Class<T>) manufacturedType;
            this.arguments = arguments;
            this.argumentTypes = new Class[arguments.length];
        }

        @SuppressWarnings("unchecked")
        public Arguments(Class<? extends T> manufacturedType, String[] arguments, Class<?>[] argumentTypes) {
            this.manufacturedType = (Class<T>) manufacturedType;
            this.arguments = arguments;
            this.argumentTypes = argumentTypes;
        }
    }


    public static class FactoryException extends RuntimeException {

        private final String key;
        private final String missingArgument;
        private final Map<String, Object> values;

        public FactoryException(Factory<?> factory, String key, String missingArgument, Map<String, Object> values) {
            super("Failure to initialize " + key + "(" + factory.map.get(key).manufacturedType.getSimpleName() + "), missing: " + missingArgument);

            this.key = key;
            this.missingArgument = missingArgument;
            this.values = values;
        }

        public String getKey() {
            return key;
        }

        public String getMissingArgument() {
            return missingArgument;
        }

        public Map<String, Object> getValues() {
            return values;
        }
    }
}
