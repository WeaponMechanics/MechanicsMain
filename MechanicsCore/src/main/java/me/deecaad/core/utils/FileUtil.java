package me.deecaad.core.utils;

import com.cjcrafter.foliascheduler.util.MinecraftVersions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarFile;

import static me.deecaad.core.MechanicsCore.debug;

/**
 * This final utility class outlines static methods that work with files, resources, and streams.
 */
public final class FileUtil {

    // Don't let anyone instantiate this class
    private FileUtil() {
    }

    /**
     * Copies the resource defined by <code>resource</code> in the project's resource folder to an
     * <code>output</code> directory. If the copied resource is a folder (If the file name does not
     * contain a '.'), then this method will be called recursively until all subdirectories are copied.
     *
     * @param source The non-null resource folder that contains the files to copy. No need to start this
     *        with a '/' character. Depending on the .jar file, you may need to append 'resources/'
     *        before your resource. Use {@link ClassLoader#getResource(String)}.
     * @param target The non-null target file to write all the resource files to. Use
     *        {@link File#toPath()}.
     *
     * @throws InternalError If an IO or URI exception occurs.
     */
    public static void copyResourcesTo(URL source, Path target) {
        if (source == null)
            throw new IllegalArgumentException("Resource was null, make sure you put in the correct path!");

        try {
            PathReference pathReference = PathReference.of(source.toURI());

            Files.walkFileTree(pathReference.path, new SimpleFileVisitor<>() {

                // "Visit" directories first so we can create the directory.
                @Override
                public @NotNull FileVisitResult preVisitDirectory(Path dir, @NotNull BasicFileAttributes attrs) throws IOException {
                    Path currentTarget = target.resolve(pathReference.path.relativize(dir).toString());
                    Files.createDirectories(currentTarget);
                    return FileVisitResult.CONTINUE;
                }

                // "Visit" each file and copy the relative path
                @Override
                public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(pathReference.path.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    public record PathReference(Path path, FileSystem fileSystem) {

        public static PathReference of(URI resource) throws IOException {
            try {
                // first try getting a path via existing file systems
                return new PathReference(Paths.get(resource), null);
            } catch (final FileSystemNotFoundException e) {
                // This generally occurs when the file is in a .jar file.
                final Map<String, ?> env = Collections.emptyMap();
                final FileSystem fs = FileSystems.newFileSystem(resource, env);
                return new PathReference(fs.provider().getPath(resource), fs);
            }

        }
    }

    /**
     * Returns the jar file from the given arguments. The file should point to a <code>.jar</code> file.
     * You can get the {@link File} from your plugin using the protected JavaPlugin#getFile() method.
     *
     * @param plugin The non-null plugin who owns the jar file.
     * @param jar The non-null file pointing to the jar
     * @return The non-null jar file.
     */
    public JarFile getJarFile(Plugin plugin, File jar) {
        if (jar == null || !jar.exists()) {
            try {
                jar = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
            } catch (SecurityException e) {
                try {
                    Method method = plugin.getClass().getDeclaredMethod("getFile");
                    method.setAccessible(true);
                    jar = (File) method.invoke(plugin);
                } catch (Exception e1) {
                    debug.log(LogLevel.ERROR, "Failed to invoke JavaPlugin#getFile()", e1);
                }
            }
            if (jar == null) {
                debug.log(LogLevel.WARN, "Could not locate " + plugin.getDescription().getName() + " jar file...");
                throw new InternalError();
            }
        }
        try {
            return new JarFile(jar);
        } catch (IOException e) {
            debug.log(LogLevel.ERROR, "Failed to create jar file: " + jar, e);
            throw new InternalError(e);
        }
    }

    /**
     * Ensures that a given <code>file</code> has all config options defined by the
     * <code>resource</code>.
     *
     * @param resource The non-null resource to copy.
     * @param file The output file that should have the default values.
     */
    public static void ensureDefaults(URL resource, File file) {
        if (resource == null)
            throw new IllegalArgumentException("Resource was null, make sure you put in the correct path!");

        // First ensure the file exists
        ensureFile(resource, file);

        // Spigot added their comment configuration stuff in 1.18
        if (!MinecraftVersions.CAVES_AND_CLIFFS_2.isAtLeast())
            return;

        YamlConfiguration from;
        try {
            from = YamlConfiguration.loadConfiguration(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new InternalError(e);
        }
        YamlConfiguration to = YamlConfiguration.loadConfiguration(file);

        // Loop through each key, and make sure it exists. If it does not, then
        // set the default value and add block/inline comments.
        boolean madeChanges = false;
        for (String key : from.getKeys(true)) {
            if (to.contains(key))
                continue;

            to.set(key, from.get(key));
            to.setComments(key, from.getComments(key));
            to.setInlineComments(key, from.getInlineComments(key));
            madeChanges = true;
        }

        // Apply/Write those changes to the file (if changes were made)
        try {
            if (madeChanges)
                to.save(file);
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }

    public static void downloadFile(File target, String link, int connectionTime, int readTime) {
        try {
            URI uri = URI.create(link);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(connectionTime * 1000);
            connection.setReadTimeout(readTime * 1000);

            InputStream in = connection.getInputStream();
            Files.copy(in, target.toPath());
            in.close();

        } catch (IOException e) {
            debug.log(LogLevel.ERROR, "Some error occurred when downloading: " + link, e);
        }
    }

    public static void ensureFile(URL resource, File file) {
        if (!file.exists()) {
            try (
                InputStream in = new BufferedInputStream(resource.openStream());
                FileOutputStream out = new FileOutputStream(file)) {
                int data;
                while ((data = in.read()) != -1) {
                    out.write(data);
                }
            } catch (IOException ex) {
                throw new InternalError(ex);
            }
        }
    }
}
