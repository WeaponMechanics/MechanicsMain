package me.deecaad.core.file;

import com.cjcrafter.foliascheduler.util.ConstructorInvoker;
import me.deecaad.core.utils.LogLevel;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import static me.deecaad.core.MechanicsCore.debug;

public class SerializerInstancer extends JarSearcher {

    public SerializerInstancer(@NotNull JarFile jar) {
        super(jar);
    }

    @SuppressWarnings("rawtypes")
    public List<Serializer<?>> createAllInstances(@NotNull ClassLoader classLoader, Class<?>... classes) {
        List<Class<Serializer>> validClasses = findAllSubclasses(Serializer.class, classLoader, true, classes);

        List<Serializer<?>> instances = new ArrayList<>(validClasses.size());
        for (Class<Serializer> validClass : validClasses) {
            Constructor<Serializer> emptyConstructor;
            try {
                emptyConstructor = validClass.getConstructor();
            } catch (NoSuchMethodException e) {
                try {
                    validClass.getDeclaredMethod("getKeyword");
                    debug.log(LogLevel.ERROR,
                        "Found a serializer that uses getKeyword() but is missing an empty constructor!",
                        "Please add empty constructor for class " + validClass.getSimpleName());
                } catch (NoSuchMethodException ex) {
                    // we can ignore this
                }

                continue;
            } catch (Throwable ex) {
                // this exception occurs when dependencies like MythicMobs are not installed
                continue;
            }

            Serializer instance = new ConstructorInvoker<>(emptyConstructor).newInstance();
            instances.add(instance);
        }

        return instances;
    }
}
