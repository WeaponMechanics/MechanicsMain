package me.deecaad.core.utils;

import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static me.deecaad.core.MechanicsCore.debug;

public class ReflectionUtil {

    private static final String versionString = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    private static final String nmsVersion = "net.minecraft.server." + versionString + '.';
    private static final String cbVersion = "org.bukkit.craftbukkit." + versionString + '.';

    private static final Field modifiersField;

    static {
        modifiersField = ReflectionUtil.getField(Field.class, "modifiers");
        modifiersField.setAccessible(true);
    }

    /**
     * Don't let anyone instantiate this class
     */
    private ReflectionUtil() {
    }

    /**
     * Tries to find class from net.minecraft.server.SERVERVERSION.className
     *
     * @param className the net minecraft server (NMS) class name to search
     * @return class object or null if not found
     */
    public static Class<?> getNMSClass(@Nonnull String className) {
        try {
            return Class.forName(nmsVersion + className);
        } catch (ClassNotFoundException e) {
            debug.log(LogLevel.ERROR, "Issue getting NMS class!", e);
            return null;
        }
    }

    /**
     * Tries to find class from org.bukkit.craftbukkit.SERVERVERSION.className
     *
     * @param className the craftbukkit class name to search
     * @return class object or null if not found
     */
    public static Class<?> getCBClass(@Nonnull String className) {
        try {
            return Class.forName(cbVersion + className);
        } catch (ClassNotFoundException e) {
            debug.log(LogLevel.ERROR, "Issue getting CB class!", e);
            return null;
        }
    }

    /**
     * @param classObject the class from where to get constructor
     * @param parameters  the params for constructor
     * @return the constructor or null if not found
     */
    public static Constructor<?> getConstructor(@Nonnull Class<?> classObject, Class<?>... parameters) {
        try {
            return classObject.getConstructor(parameters);
        } catch (NoSuchMethodException | SecurityException e) {
            debug.log(LogLevel.ERROR, "Issue getting constructor!", e);
            return null;
        }
    }

    /**
     * Instantiates new object with given constructor and params
     *
     * @param constructor the constructor to construct
     * @param parameters  the params for constructor (must match)
     * @return the new instance as object
     */
    public static Object newInstance(@Nonnull Constructor<?> constructor, Object... parameters) {
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            debug.log(LogLevel.ERROR, "Issue creating new instance!", e);
            return null;
        }
    }

    /**
     * Gets a new instance of a class using the default constructor
     *
     * @param clazz The class to instantiate
     * @return Instantiated object
     */
    public static <T> T newInstance(@Nonnull Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param classObject the class from where to get field
     * @param fieldName   the field name in class
     * @return the field or null if not found
     */
    public static Field getField(@Nonnull Class<?> classObject, @Nonnull String fieldName) {
        try {
            Field field = classObject.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            debug.log(LogLevel.ERROR, "Issue getting field!", e);
            return null;
        }
    }

    /**
     * Gets the field by it's datatype instead of it's name. Useful
     * for obfuscated code where the variable name changes but the
     * type of the variables does not.
     *
     * @param target The class to pull the field from (Or from any of it's superclasses)
     * @param name   The name of the field, or null for any name
     * @param type   The type the field must have
     * @return The found field
     * @throws IllegalArgumentException When no such field exists
     */
    public static Field getField(@Nonnull Class<?> target, @Nullable String name, Class<?> type) {
        return getField(target, name, type, 0);
    }

    public static Field getField(@Nonnull Class<?> target, @Nullable String name, Class<?> type, int index) {
        for (final Field field : target.getDeclaredFields()) {

            // Check if the name field, if the name is not a wildcard
            if (name == null || name.equals(field.getName())) {

                // Type check. Make sure the field's datatype
                // matches the data type we are trying to find
                if (!type.isAssignableFrom(field.getType()))
                    continue;
                else if (index-- > 0)
                    continue;

                if (!field.isAccessible())
                    field.setAccessible(true);

                return field;
            }
        }

        // if the class has a superclass, then recursively check
        // the super class for the field
        Class<?> superClass = target.getSuperclass();
        if (superClass != null)
            return getField(superClass, name, type);

        throw new IllegalArgumentException("Cannot find field with type " + type);
    }

    /**
     * Sets the field to be a non final field
     *
     * @param field The field to set
     * @return The field
     */
    public static Field setFieldModifiable(Field field) {
        try {
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException ex) {
            debug.log(LogLevel.ERROR, "Issue changing final field to non-final!", ex);
        }
        return field;
    }

    /**
     * @param field    the field to get value from
     * @param instance the instance holding field (null in static use)
     * @return the field object or null if not found
     */
    public static Object invokeField(@Nonnull Field field, @Nullable Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            debug.log(LogLevel.ERROR, "Issue invoking field!", e);
            return null;
        }
    }

    /**
     * @param field    the field to set new value
     * @param instance the instance holding field (null in static use)
     * @param value    the new value for field
     */
    public static void setField(@Nonnull Field field, @Nullable Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            debug.log(LogLevel.ERROR, "Issue setting field!", e);
        }
    }

    /**
     * @param classObject the class from where to get method
     * @param methodName  the method name in class
     * @param parameters  the params for method
     * @return the method or null if not found
     */
    public static Method getMethod(@Nonnull Class<?> classObject, @Nonnull String methodName, Class<?>... parameters) {
        try {
            Method method = classObject.getDeclaredMethod(methodName, parameters);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method;
        } catch (NoSuchMethodException | SecurityException e) {
            debug.log(LogLevel.ERROR, "Issue getting method!", e);
            return null;
        }
    }

    public static Method getMethod(@Nonnull Class<?> target, @Nullable Class<?> returnType, Class<?>... params) {
        for (final Method method : target.getDeclaredMethods()) {
            if (returnType != null && !returnType.isAssignableFrom(method.getReturnType()))
                continue;
            else if (!Arrays.equals(method.getParameterTypes(), params))
                continue;

            if (!method.isAccessible())
                method.setAccessible(true);

            return method;
        }

        // Recursively check superclasses for the method
        if (target.getSuperclass() != null)
            return getMethod(target.getSuperclass(), returnType, params);

        throw new IllegalArgumentException("Cannot find field with return=" + returnType
                + ", params=" + Arrays.toString(params));
    }

    /**
     * @param method     the method to modify
     * @param instance   the instance used to invoke method (null in static use)
     * @param parameters the parmas of method
     * @return the method object or null if not found or null if method is for e.g void
     */
    public static Object invokeMethod(@Nonnull Method method, Object instance, Object... parameters) {
        try {
            return method.invoke(instance, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            debug.log(LogLevel.ERROR, "Issue invoking method!", e);
            return null;
        }
    }
}