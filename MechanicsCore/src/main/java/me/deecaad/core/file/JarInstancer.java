package me.deecaad.core.file;

import com.cjcrafter.foliascheduler.util.ConstructorInvoker;
import me.deecaad.core.utils.LogLevel;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import static me.deecaad.core.MechanicsCore.debug;

public class JarInstancer extends JarSearcher {

    public JarInstancer(@NotNull JarFile jar) {
        super(jar);
    }

    public <T> List<T> createAllInstances(Class<T> clazz, ClassLoader classLoader, boolean isIgnoreAbstract, Class<?>... classes) {
        List<Class<T>> validClasses = findAllSubclasses(clazz, classLoader, isIgnoreAbstract, classes);

        List<T> instances = new ArrayList<>();
        for (Class<T> validClass : validClasses) {
            Constructor<T> emptyConstructor;
            try {
                emptyConstructor = validClass.getConstructor();
            } catch (NoSuchMethodException e) {
                debug.log(LogLevel.ERROR,
                    "Found an class implementing " + clazz.getSimpleName() + " class which didn't have empty constructor!",
                    "Please add empty constructor for class " + validClass.getSimpleName());
                continue;
            }

            T instance = new ConstructorInvoker<>(emptyConstructor).newInstance();
            instances.add(instance);
        }

        if (instances.isEmpty())
            debug.warn("Did not instantiate anything? For " + clazz);
        return instances;
    }
}