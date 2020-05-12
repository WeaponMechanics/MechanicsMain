package me.deecaad.core.utils;

import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static me.deecaad.core.MechanicsCore.debug;

public class ReflectionUtil {

    private static final String versionString = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    
    /**
     * Don't let anyone instantiate this class
     */
    private ReflectionUtil() { }
    
    /**
     * Tries to find class from net.minecraft.server.SERVERVERSION.className
     *
     * @param className the net minecraft server (NMS) class name to search
     * @return class object or null if not found
     */
    public static Class<?> getNMSClass(@Nonnull String className) {
        try {
            return Class.forName("net.minecraft.server." + versionString + "." + className);
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
            return Class.forName("org.bukkit.craftbukkit." + versionString + "." + className);
        } catch (ClassNotFoundException e) {
            debug.log(LogLevel.ERROR, "Issue getting CB class!", e);
            return null;
        }
    }

    /**
     * @param classObject the class from where to get constructor
     * @param parameters the params for constructor
     * @return the constructor or null if not found
     */
    public static Constructor<?> getConstructor(@Nonnull Class<?> classObject, Class<?>... parameters) {
        if (classObject == null) {
            debug.log(LogLevel.WARN, "classObject is null in getConstructor()");
            return null;
        }
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
     * @param parameters the params for constructor (must match)
     * @return the new instance as object
     */
    public static Object newInstance(@Nonnull Constructor<?> constructor, Object... parameters) {
        if (constructor == null) {
            debug.log(LogLevel.WARN, "constructor is null in newInstance()!");
            return null;
        }
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            debug.log(LogLevel.ERROR, "Issue creating new instance!", e);
            return null;
        }
    }

    /**
     * @param classObject the class from where to get field
     * @param fieldName the field name in class
     * @return the field or null if not found
     */
    public static Field getField(@Nonnull Class<?> classObject, @Nonnull String fieldName) {
        if (classObject == null || fieldName == null) {
            debug.log(LogLevel.WARN, "classObject or fieldName is null in getField()");
            return null;
        }
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
     * @param field the field to get value from
     * @param instance the instance holding field (null in static use)
     * @return the field object or null if not found
     */
    public static Object invokeField(@Nonnull Field field, @Nonnull Object instance) {
        if (field == null || instance == null) {
            debug.log(LogLevel.WARN, "field or instance is null in invokeField()");
            return null;
        }
        try {
            return field.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            debug.log(LogLevel.ERROR, "Issue invoking field!", e);
            return null;
        }
    }

    /**
     * @param field the field to set new value
     * @param instance the instance holding field (null in static use)
     * @param value the new value for field
     */
    public static void setField(@Nonnull Field field, Object instance, Object value) {
        if (field == null || instance == null) {
            debug.log(LogLevel.WARN, "field or instance is null in setField()");
        }
        try {
            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            debug.log(LogLevel.ERROR, "Issue setting field!", e);
        }
    }

    /**
     * @param classObject the class from where to get method
     * @param methodName the method name in class
     * @param parameters the params for method
     * @return the method or null if not found
     */
    public static Method getMethod(@Nonnull Class<?> classObject, @Nonnull String methodName, Class<?>... parameters) {
        if (classObject == null || methodName == null) {
            debug.log(LogLevel.WARN, "classObject or methodName is null in getMethod()");
            return null;
        }
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

    /**
     * @param method the method to modify
     * @param instance the instance used to invoke method (null in static use)
     * @param parameters the parmas of method
     * @return the method object or null if not found or null if method is for e.g void
     */
    public static Object invokeMethod(@Nonnull Method method, Object instance, Object... parameters) {
        if (method == null) {
            debug.log(LogLevel.WARN, "method is null in invokeField()");
            return null;
        }
        try {
            return method.invoke(instance, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            debug.log(LogLevel.ERROR, "Issue invoking method!", e);
            return null;
        }
    }
}