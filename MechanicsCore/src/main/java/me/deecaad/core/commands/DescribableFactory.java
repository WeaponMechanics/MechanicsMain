package me.deecaad.core.commands;

import com.cjcrafter.foliascheduler.util.ConstructorInvoker;
import com.cjcrafter.foliascheduler.util.ReflectionUtil;
import com.cjcrafter.foliascheduler.util.WrappedReflectiveOperationException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 *
 */
public class DescribableFactory {

    /**
     * Makes an argument describable by adding the {@link Describable} interface and implementing the
     * {@link Describable#getDescription()} method. This method will copy all fields from the original
     * instance to the new instance.
     *
     * @param instance The instance to make describable
     * @param description The description of the instance
     * @return The new instance
     * @param <T> The type of the argument
     */
    public static <T> @NotNull T makeArgumentDescribable(@NotNull T instance, @NotNull String description) {
        Class<T> baseClass = (Class<T>) instance.getClass();
        Class<? extends T> clazz = new ByteBuddy()
            .subclass(baseClass)
            .implement(Describable.class)
            .defineMethod("getDescription", String.class, net.bytebuddy.description.modifier.Visibility.PUBLIC)
            .intercept(FixedValue.value(description))
            .make()
            .load(instance.getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();

        // Assume jorel's arguments always have a constructor that takes 1 string <nodename>
        ConstructorInvoker<? extends T> constructor = ReflectionUtil.getConstructor(clazz, String.class);
        T newInstance = constructor.newInstance("dummy"); // this gets replaced later by field copying

        // Copy all fields from the original instance to the new instance. This handles the
        // other important fields, like suggestions and types.
        for (Field field : baseClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                field.set(newInstance, field.get(instance));
            } catch (IllegalAccessException e) {
                throw new WrappedReflectiveOperationException(e);
            }
        }

        return newInstance;
    }
}
