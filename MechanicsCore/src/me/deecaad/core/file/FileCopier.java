package me.deecaad.core.file;

import me.deecaad.core.utils.LogLevel;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class FileCopier {

    /**
     * Creates resources inside path of plugin file into plugin's datafolder
     *
     * @param plugin the plugin instance
     * @param file the jar file of plugin
     * @param path the path inside plugin jar where resources are
     * @param fileTypes the required file types
     */
    public void createFromJarToDataFolder(Plugin plugin, File file, String path, String... fileTypes) {
        if (plugin.getDataFolder() == null || !plugin.getDataFolder().exists() || plugin.getDataFolder().listFiles().length <= 0) {
            createFromJar(plugin, file, path, null, fileTypes);
        }
    }

    /**
     * Creates resources inside path of plugin file into given copy path
     *
     * @param plugin the plugin instance
     * @param jar the jar file of plugin
     * @param path the path inside plugin jar where resources are
     * @param copyToPath the path where to copy files
     * @param fileTypes the required file types
     */
    public void createFromJar(Plugin plugin, File jar, String path, String copyToPath, String... fileTypes) {
        try {
            JarFile jarFile = getJarFile(plugin, jar);
            if (jarFile == null) {
                return;
            }
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!entryName.startsWith(path)) {
                    continue;
                }
                if (fileTypes != null && fileTypes.length > 0) {
                    boolean one = false;
                    for (String type : fileTypes) {
                        if (entryName.endsWith(type)) {
                            one = true;
                            break;
                        }
                    }
                    if (!one) {
                        continue;
                    }
                }
                String dataPath = entryName.replace(path, "");
                String newFilePath = (copyToPath != null ? copyToPath : plugin.getDataFolder()) + dataPath;
                File newFile = new File(newFilePath);
                if (newFile.exists()) {
                    continue;
                }
                File parentFile = newFile.getParentFile();
                if (parentFile == null) {
                    String[] splitPath = newFilePath.split("/");
                    parentFile = new File(newFilePath.replace("/" + splitPath[splitPath.length - 1], ""));
                }
                boolean failed = parentFile.mkdirs() || newFile.createNewFile();
                debug.validate(!failed, "Failed to make directory at either: ", parentFile.toString(), newFile.toString());

                copy(plugin.getResource(entryName), newFile);
            }
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to get the jar file of plugin manually if given jar is not valid.
     * Its better to give the jar file for methods above from main class (use getFile() method there).
     *
     * @param plugin the plugin instance
     * @param jar the jar as file
     * @return the jar file of plugin
     */
    public JarFile getJarFile(Plugin plugin, File jar) throws IOException {
        if (jar == null || !jar.exists()) {
            try {
                jar = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
            } catch (SecurityException e) {
                try {
                    Method method = plugin.getClass().getDeclaredMethod("getFile");
                    method.setAccessible(true);
                    jar = (File) method.invoke(plugin);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (jar == null) {
                debug.log(LogLevel.WARN,
                        "Could not locate " + plugin.getDescription().getName() + " jar file...");
                return null;
            }
        }
        return new JarFile(jar);
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
