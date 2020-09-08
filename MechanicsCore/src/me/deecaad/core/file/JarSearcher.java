package me.deecaad.core.file;


import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class JarSearcher {

    private JarFile jar;

    public JarSearcher(JarFile jar) {
        if (jar == null) {
            throw new IllegalArgumentException("Cannot search a null jar!");
        }

        this.jar = jar;
    }

    @SuppressWarnings("unchecked")
    public <T> List<Class<T>> findAllSubclasses(@Nonnull Class<T> clazz, boolean isIgnoreAbstract, Class<?>...classes) {
        List<Class<?>> classList = new ArrayList<>(Arrays.asList(classes));
        classList.add(clazz);
        Set<String> blacklist = classList.stream()
                .map(Class::getName)
                .map(str -> str.replaceAll("\\.", "/") + ".class")
                .collect(Collectors.toSet());


        Enumeration<JarEntry> entries = jar.entries();
        List<Class<T>> subclasses = new ArrayList<>();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            // Validate that the entry is a class, and that it is not blacklisted
            if (!entryName.endsWith(".class") || blacklist.contains(entryName)) {
                continue;
            }

            String name = entryName.replaceAll("/", "\\.").replace(".class", "");
            Class<?> subclass;
            try {
                subclass = Class.forName(name);
            } catch (ClassNotFoundException | NoClassDefFoundError ex) {
                continue;
            }

            // Check for inheritance and abstraction
            int mod = subclass.getModifiers();
            if (!clazz.isAssignableFrom(subclass)) {
                continue;
            } else if (isIgnoreAbstract && (Modifier.isAbstract(mod) || Modifier.isInterface(mod))) {
                continue;
            }

            subclasses.add((Class<T>) subclass);
        }

        return subclasses;
    }
}
