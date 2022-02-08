package me.deecaad.core.file;


import me.deecaad.core.utils.StringUtil;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * This immutable class outlines a searcher that iterates through the files of
 * a {@link JarFile}.
 */
public class JarSearcher {

    private final JarFile jar;

    /**
     * This constructor will throw an {@link IllegalArgumentException} if the
     * given <code>jar</code> is null. {@link JarFile} instances can be
     * obtained via {@link FileCopier#getJarFile(Plugin, File)}.
     *
     * @param jar The <code>.jar</code> file to search.
     */
    public JarSearcher(@Nonnull JarFile jar) {
        if (jar == null) {
            throw new IllegalArgumentException("Cannot search a null jar!");
        }

        this.jar = jar;
    }

    /**
     * Returns a {@link List} of every class that inherits from the given class
     * <code>clazz</code> that can be found in this searcher's {@link JarFile}.
     *
     * <p>This is useful if addons to a plugin create an new implementation for a
     * feature, so the main plugin doesn't have to write a registry system for
     * each feature. Instead, the main plugin can pull all features from
     * registered jars.
     *
     * @param clazz            The class that the subclasses inherit from. This
     *                         is generally an abstract class/interface.
     * @param clazzLoader      The class that is used to load classes
     * @param isIgnoreAbstract If this is <code>true</code>, then any subclass
     *                         that is an interface or is an abstract class
     *                         will not be included in the returned list.
     * @param classes          Class blacklist. Any classes included in this
     *                         array will not be included in the returned list.
     * @param <T>              The parent class that all of these subclasses
     *                         will have in common.
     * @return A {@link List} of every subclass.
     */
    @SuppressWarnings("unchecked")
    public <T> List<Class<T>> findAllSubclasses(@Nonnull Class<T> clazz, ClassLoader clazzLoader, boolean isIgnoreAbstract, Class<?>... classes) {

        if (clazz == null) throw new IllegalArgumentException("clazz cannot be null");

        // Create the class blacklist. The class "clazz" and any classes listed
        // from "classes" are added to the blacklist. This prevents the class
        // from being loaded, and the list will not be added to the returned
        // list.
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

            // Determine if this entry is a class file, and it is not contained
            // on the class blacklist.
            if (!entryName.endsWith(".class") || blacklist.contains(entryName)) {
                continue;
            }

            String name = entryName.replaceAll("/", "\\.").replace(".class", "");

            // TODO
            // Find better way to address loading compatibility versions
            if (true) {

                if (name.contains("compatibility")) {
                    //System.out.println("hihihi");
                }

                // matches a string like me.deecaad.weaponmechanics.compatibility.nbt.NBT_1_16_R3
                String regex = ".+compatibility.+_?\\d_\\d+_R\\d";
                if (StringUtil.match(regex, name) != null) {
                    continue;
                }
            }

            Class<?> subclass;
            try {
                subclass = Class.forName(name, false, clazzLoader);
            } catch (ClassNotFoundException | NoClassDefFoundError | UnsupportedClassVersionError ex) {
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
