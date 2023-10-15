package me.deecaad.core.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Arrays;

/**
 * This final utility class outlines static methods that operate on or return
 * members of the {@link java.lang.reflect} package. This class also deals with
 * version compatibility.
 *
 * <p>The methods of this class are threadsafe. For most of the methods, if an
 * error occurs inside of the method, it will return <code>null</code>.
 */
@SuppressWarnings("unused")
public final class ReflectionUtil {

    private static final String versionString;
    private static final String nmsVersion;
    private static final String cbVersion;

    private static final Field modifiersField;
    private static final int javaVersion;
    private static final int mcVersion;

    private static final String ERR = "This is probably caused by your minecraft server version. Contact a DEV for more help.";

    static {
        int javaVersion1;

        // Occurs when run without a server (e.x. In intellij)
        //noinspection ConstantConditions
        if (Bukkit.getServer() == null) {
            versionString = "TESTING";
            mcVersion = -1;
        } else {
            versionString = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            mcVersion = Integer.parseInt(versionString.split("_")[1]);
        }

        nmsVersion = "net.minecraft.server." + versionString + '.';
        cbVersion = "org.bukkit.craftbukkit." + versionString + '.';

        try {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.")) {
                version = version.substring(2, 3);
            } else {
                int dot = version.indexOf(".");
                if (dot != -1)
                    version = version.substring(0, dot);
            }
            // IF version is something like 18-ea ->
            version = version.split("-")[0];

            javaVersion1 =  Integer.parseInt(version);
        } catch (Throwable throwable) {
            javaVersion1 = -1;
            throwable.printStackTrace();
        }
        javaVersion = javaVersion1;

        if (javaVersion < 12) {
            modifiersField = getField(Field.class, "modifiers");
        } else {
            // todo modify final fields post java 12
            modifiersField = null;
        }
    }

    // Don't let anyone instantiate this class
    private ReflectionUtil() {
    }

    public static int getMCVersion() {
        return mcVersion;
    }

    /**
     * Returns the major java version. For java versions that start with a one
     * (E.x. <samp>1.8</samp>), this method will return 8. For newer java
     * versions, this method will return the major version.
     *
     * @return The non-negative java version.
     */
    public static int getJavaVersion() {
        return javaVersion;
    }

    /**
     * Returns the NMS class with the given name.
     * In mc versions 1.17 and higher, <code>pack</code> is used for the
     * package the class is in. Previous versions ignore <code>pack</code>.
     *
     * @param pack The non-null package name that contains the class defined
     *             by <code>name</code>. Make sure the string ends with a dot.
     * @param name The non-null name of the class to find.
     * @return The NMS class with that name.
     */
    public static Class<?> getNMSClass(@NotNull String pack, @NotNull String name) {
        String className;

        if (getMCVersion() < 17)
            className = nmsVersion + name;
        else
            className = "net.minecraft." + pack + '.' + name;

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalError("Failed to get NMS class " + className + ". " + ERR, e);
        }
    }

    /**
     * Returns the net.minecraft.network.protocol.game packet for the
     * given class name in 1.17+, or the NMS packet
     * for older versions.
     *
     * @param className The non-null name of the class to get.
     * @return The NMS class with that name, or <code>null</code>.
     */
    @NotNull
    public static Class<?> getPacketClass(@NotNull String className) {
        return getNMSClass("network.protocol.game", className);
    }

    /**
     * Returns the CraftBukkit class with the given package
     * and name.
     *
     * @param className The non-null name of the class to get.
     * @return The CB class with that name, or null.
     */
    @NotNull
    public static Class<?> getCBClass(@NotNull String className) {
        try {
            return Class.forName(cbVersion + className);
        } catch (ClassNotFoundException e) {
            throw new InternalError("Failed to get CB class " + className + ". " + ERR, e);
        }
    }

    /**
     * Returns the {@link Constructor} of the given <code>classObject</code>
     * that matches the given parameters.
     *
     * @param clazz      The class to get the constructor from.
     * @param parameters The types of parameters that the constructor takes.
     * @return The found constructor, or <code>null</code>.
     */
    @NotNull
    public static <T> Constructor<T> getConstructor(@NotNull Class<T> clazz, Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new InternalError("Failed to get constructor. " + ERR, e);
        }
    }

    /**
     * Instantiates a new {@link Object} of the generic class time defined by
     * <code>constructorSupplier</code>.
     *
     * @param constructorSupplier The class to instantiate.
     * @param parameters          The parameters of the constructor to use.
     * @param <T>                 The generic class type to return.
     * @return A new object of the given class.
     * @see #newInstance(Constructor, Object...)
     */
    @NotNull
    public static <T> T newInstance(@NotNull Class<T> constructorSupplier, Object... parameters) {
        Class<?>[] classes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            classes[i] = parameters[i].getClass();

            classes[i] = switch (parameters[i].getClass().getSimpleName()) {
                case "Double" -> double.class;
                case "Integer" -> int.class;
                case "Float" -> float.class;
                case "Boolean" -> boolean.class;
                case "Byte" -> byte.class;
                case "Short" -> short.class;
                case "Long" -> long.class;
                default -> classes[i];
            };
        }

        try {
            return newInstance(constructorSupplier.getConstructor(classes), parameters);
        } catch (NoSuchMethodException e) {
            throw new InternalError("Failed to instantiate class " + constructorSupplier + ". " + ERR, e);
        }
    }

    /**
     * Instantiates a new {@link Object} using the given
     * <code>constructor</code> and <code>parameters</code>. If the constructor
     * is a default constructor, <code>parameters.length</code> should equal 0.
     *
     * @param constructor The constructor to use to instantiate the object.
     * @param parameters  The parameters that the constructor takes.
     * @return The new object, or null.
     */
    @NotNull
    public static <T> T newInstance(@NotNull Constructor<T> constructor, Object... parameters) {
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new InternalError("Failed to instantiate class " + constructor + ". " + ERR, e);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> Class<T> getClass(@NotNull String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalError("Failed to find class with name " + className + ". " + ERR, e);
        }
    }

    /**
     * Instantiates a new {@link Object} of the generic class type using the
     * default constructor. If the class does not have a default constructor,
     * this method will return <code>null</code>.
     *
     * @param clazz The class to instantiate a new instance from.
     * @param <T>   The generic type of the class.
     * @return The new instance, or <code>null</code>.
     */
    @NotNull
    public static <T> T newInstance(@NotNull Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new InternalError("Failed to instantiate class " + clazz + ". " + ERR, e);
        }
    }

    /**
     * Returns the {@link Field} belonging to the <code>classObject</code> with
     * the given <code>fieldName</code>. If no such field exists, this method
     * will return <code>null</code>.
     *
     * <p>After calling this method, you should cache the returned field to
     * avoid the overhead of searching for a field every time you want to use
     * it. If the field you are getting is obfuscated, it is a good practice
     * to use {@link #getField(Class, Class, int)} instead, which will
     * likely be more accurate across server versions.
     *
     * @param clazz     The non-null class to pull the field from.
     * @param fieldName The non-null name of the field to pull.
     * @return The found field.
     */
    @NotNull
    public static Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);

            //noinspection deprecation
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            throw new InternalError("Failed to get field " + fieldName + ". " + ERR, e);
        }
    }

    /**
     * Returns a {@link Field} by it's datatype. This method should be used for
     * getting obfuscated fields, or if the name of the {@link Field} is
     * otherwise not guaranteed to stay the same.
     *
     * If the <code>target</code> does not declare a matching field, this
     * method will search in the parent class.
     *
     * @param target The class to get the field from.
     * @param type   The non-null datatype of the field.
     * @return The non-null found field.
     * @throws IllegalArgumentException If no such field exists.
     */
    public static Field getField(@NotNull Class<?> target, Class<?> type) {
        return getField(target, type, 0, false);
    }

    /**
     * Returns a {@link Field} by it's datatype. This method should be used for
     * getting obfuscated fields, or if the name of the {@link Field} is
     * otherwise not guaranteed to stay the same.
     *
     * If the <code>target</code> does not declare a matching field, this
     * method will search in the parent class.
     *
     * @param target The class to get the field from.
     * @param type   The non-null datatype of the field.
     * @param index  The index of the field. Sometimes this method will match
     *               multiple fields. For these fields, <code>index</code> is
     *               required.
     * @return The non-null found field.
     * @throws IllegalArgumentException If no such field exists.
     */
    public static Field getField(@NotNull Class<?> target, Class<?> type, int index) {
        return getField(target, type, index, false);
    }

    public static Field getField(@NotNull Class<?> target, Class<?> type, int index, boolean skipStatic) {
        for (final Field field : target.getDeclaredFields()) {

            // Type check. Make sure the field's datatype
            // matches the data type we are trying to find
            if (!type.isAssignableFrom(field.getType()))
                continue;
            if (skipStatic && Modifier.isStatic(field.getModifiers()))
                continue;
            if (index-- > 0)
                continue;

            //noinspection deprecation
            if (!field.isAccessible())
                field.setAccessible(true);

            return field;
        }

        // if the class has a superclass, then recursively check
        // the super class for the field
        Class<?> superClass = target.getSuperclass();
        if (superClass != null)
            return getField(superClass, type, index);

        throw new IllegalArgumentException("Cannot find field with type " + type);
    }

    /**
     * Returns the value of the <code>field</code>.
     *
     * @param field    The non-null field that holds the value.
     * @param instance The instance that holds the field, or <code>null</code>
     *                 for static fields.
     * @return The value of the field, or <code>null</code>.
     */
    public static Object invokeField(@NotNull Field field, @Nullable Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InternalError("Failed to invoke field " + field + ". " + ERR, e);
        }
    }

    /**
     * Sets the value of the <code>field</code> for a given instance. For
     * static fields, <code>instance</code> should be <code>null</code>. This
     * method does not work for static final fields.
     *
     * @param field    The non-null field to set the value of.
     * @param instance The object to set the field value to. For static fields,
     *                 this should be <code>null</code>.
     * @param value    The value to set to the field.
     */
    public static void setField(@NotNull Field field, @Nullable Object instance, Object value) {
        try {

            // TODO This does not work yet. static final fields are tough
            if (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                if (javaVersion < 12) {

                    // Not sure why, but this does not allow modifying static final fields
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                }
            }

            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InternalError("Failed to set field " + field + ". " + ERR, e);
        }
    }

    /**
     * Returns the {@link Method} declared in the given <code>clazz</code> with
     * a signature that matches <code>methodName</code> and
     * <code>parameters</code>.
     *
     * @param clazz      The non-null class that declares the method.
     * @param methodName The non-null name of the method.
     * @param parameters The non-null parameters of the method.
     * @return The method that matches the given signature, or
     *         <code>null</code>.
     */
    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... parameters) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameters);

            //noinspection deprecation
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new InternalError("Failed to find  method " + methodName + ". " + ERR, e);
        }
    }

    /**
     * Returns a {@link Method} by it's returned datatype and parameters. This
     * method should be used for getting obfuscated methods, or if the name of
     * the method is otherwise not guaranteed to stay the same.
     *
     * If the <code>target</code> does not declare a matching method, this
     * method will search in the parent class recursively.
     *
     * @param target     The non-null target class that declares the method.
     * @param returnType The nullable returned type of the method.
     * @param params     The non-null parameters of of the method.
     * @return The non-null method that matches the given signature.
     * @throws IllegalArgumentException If no such method exists.
     */
    public static Method getMethod(@NotNull Class<?> target, @Nullable Class<?> returnType, Class<?>... params) {
        return getMethod(target, returnType, 0, params);
    }

    /**
     * Returns a {@link Method} by it's returned datatype and parameters. This
     * method should be used for getting obfuscated methods, or if the name of
     * the method is otherwise not guaranteed to stay the same.
     *
     * If the <code>target</code> does not declare a matching method, this
     * method will search in the parent class recursively.
     *
     * @param target     The non-null target class that declares the method.
     * @param returnType The nullable returned type of the method.
     * @param index      The index of the method. Sometimes this method will
     *                   match multiple methods. For these methods,
     *                   <code>index</code> is required.
     * @param params     The non-null parameters of of the method.
     * @return The non-null method that matches the given signature.
     * @throws IllegalArgumentException If no such method exists.
     */
    public static Method getMethod(@NotNull Class<?> target, @Nullable Class<?> returnType, int index, Class<?>... params) {
        for (final Method method : target.getDeclaredMethods()) {
            if (returnType != null && !returnType.isAssignableFrom(method.getReturnType()))
                continue;
            else if (!Arrays.equals(method.getParameterTypes(), params))
                continue;
            else if (index-- > 0)
                continue;

            //noinspection deprecation
            if (!method.isAccessible())
                method.setAccessible(true);

            return method;
        }

        // Recursively check superclasses for the method
        if (target.getSuperclass() != null)
            return getMethod(target.getSuperclass(), returnType, index, params);

        throw new IllegalArgumentException("Cannot find field with return=" + returnType
                + ", params=" + Arrays.toString(params));
    }

    /**
     * Invokes the given <code>method</code>, running it then returning the
     * method's returned value. The method is run as a member of
     * <code>instance</code>, with <code>parameters</code>.
     *
     * @param method     The non-null method to run.
     * @param instance   The instance to run the method as a part of. For
     *                   static methods, this should be <code>null</code>.
     * @param parameters The parameters of the method.
     * @return The returned value from the method, or <code>null</code>.
     */
    public static Object invokeMethod(@NotNull Method method, Object instance, Object... parameters) {
        try {
            return method.invoke(instance, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new InternalError("Failed to invoke method " + method + ". " + ERR, e);
        }
    }
}