package me.deecaad.core.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;

/**
 * This final utility class outlines static methods that work with files,
 * resources, and streams.
 */
public final class FileUtil {

    // Don't let anyone instantiate this class
    private FileUtil() {
    }

    /**
     * Copies the resource defined by <code>resource</code> in the project's
     * resource folder to an <code>output</code> directory. If the copied
     * resource is a folder (If the file name does not contain a '.'), then
     * this method will be called recursively until all subdirectories are
     * copied.
     *
     * @param source The non-null resource folder that contains the files to
     *               copy. No need to start this with a '/' character.
     *               Depending on the .jar file, you may need to append
     *               'resources/' before your resource. Use
     *               {@link ClassLoader#getResource(String)}.
     * @param target The non-null target file to write all the resource files
     *               to. Use {@link File#toPath()}.
     *
     * @throws InternalError If an IO or URI exception occurs.
     */
    public static void copyResourcesTo(URL source, Path target) {
        try {
            PathReference pathReference = PathReference.of(source.toURI());

            Files.walkFileTree(pathReference.getPath(), new SimpleFileVisitor<Path>() {

                // "Visit" directories first so we can create the directory.
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path currentTarget = target.resolve(pathReference.getPath().relativize(dir).toString());
                    Files.createDirectories(currentTarget);
                    return FileVisitResult.CONTINUE;
                }

                // "Visit" each file and copy the relative path
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(pathReference.getPath().relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    public static class PathReference {
        private final Path path;
        private final FileSystem fileSystem;

        private PathReference(Path path, FileSystem fileSystem) {
            this.path = path;
            this.fileSystem = fileSystem;
        }

        public Path getPath() {
            return path;
        }

        public FileSystem getFileSystem() {
            return fileSystem;
        }

        public static PathReference of(URI resource) throws IOException {
            try
            {
                // first try getting a path via existing file systems
                return new PathReference(Paths.get(resource), null);
            }
            catch (final FileSystemNotFoundException e)
            {
                // This generally occurs when the file is in a .jar file.
                final Map<String, ?> env = Collections.emptyMap();
                final FileSystem fs = FileSystems.newFileSystem(resource, env);
                return new PathReference(fs.provider().getPath(resource), fs);
            }

        }
    }

    /**
     * Ensures that a given <code>file</code> has all config options
     * defined by the <code>resource</code>.
     *
     * @param loader   The non-null loading plugin's class loader.
     * @param resource The non-null name of the resource to copy.
     * @param file     The output file that should have the default values.
     */
    public static void ensureDefaults(ClassLoader loader, String resource, File file) {

        Yaml yaml = new Yaml();
        InputStream input;
        try {
            URL url = loader.getResource(resource);
            if (url == null) {
                throw new InternalError("Unknown resource: " + resource);
            }

            input = url.openStream();
        } catch (IOException e) {
            throw new InternalError(e);
        }

        // If the file does not exist, just write a new file.
        if (!file.exists()) {

            try (FileOutputStream output = new FileOutputStream(file)) {
                if (!file.createNewFile()) {
                    throw new InternalError("Failed to create new file " + file);
                }

                int data;
                while ((data = input.read()) != -1) {
                    output.write(data);
                }
            } catch (IOException e) {
                throw new InternalError(e);
            }
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> defaults = yaml.load(input);

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!config.contains(key)) {
                config.set(key, value);
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }

    public static void downloadFile(File target, String link, int connectionTime, int readTime) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(link).openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(connectionTime * 1000);
            connection.setReadTimeout(readTime * 1000);

            InputStream in = connection.getInputStream();
            Files.copy(in, target.toPath());
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ensureFile(ClassLoader loader, String resource, File file) {
        if (!file.exists()) {
            try (
                    InputStream in = loader.getResourceAsStream(resource);
                    FileOutputStream out = new FileOutputStream(file);
            ) {
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
