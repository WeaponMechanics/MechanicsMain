package me.deecaad.core.web;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SpigotResource {

    private final Plugin plugin;
    private URL latestURL;

    private String remoteVersion;

    public SpigotResource(Plugin plugin, String resourceId) {
        this.plugin = plugin;
        try {
            this.latestURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates latest version based on resource ID.
     */
    public void update() {
        try {
            HttpURLConnection connection = (HttpURLConnection) this.latestURL.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", plugin.getDescription().getName());
            remoteVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
        } catch (IOException e) {
            if (e.toString().contains("Server returned HTTP response code: 400")) {
                return;
            }
            e.printStackTrace();
        }
    }

    /**
     * @return the local version
     */
    @Nonnull
    public String getLocalVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * @return the remote version
     */
    @Nonnull
    public String getRemoveVersion() {
        if (remoteVersion == null) {
            return "Unknown";
        }
        return remoteVersion;
    }

    /**
     * @return the plugin object used in this spigot resource
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * @return the amount of major versions server is currently behind
     */
    public int getMajorVersionsBehind() {
        return getVersionsBehind(0);
    }

    /**
     * @return the amount of minor versions server is currently behind
     */
    public int getMinorVersionsBehind() {
        if (getMajorVersionsBehind() != 0)
            return 0;

        return getVersionsBehind(1);
    }

    /**
     * @return the amount of patch versions server is currently behind
     */
    public int getPatchVersionsBehind() {
        if (getMinorVersionsBehind() != 0)
            return 0;

        return getVersionsBehind(2);
    }

    /**
     * Simple method to check how many versions server is behind based on index.
     * 0 = major, 1 = minor, 2 = patch
     */
    private int getVersionsBehind(int index) {
        if (remoteVersion == null || remoteVersion.equalsIgnoreCase("Failed")) return 0;

        // Works for versions like "v1.33.753-BETA"
        // -> 1, 33, 753
        // Lazy implementation for splits (can be changed to regex, but performance isn't priority here)
        int local = Integer.parseInt(plugin.getDescription().getVersion().replaceFirst("v", "").split(" ")[0].split("-")[0].split("\\.")[index]);
        try {
            int remote = Integer.parseInt(remoteVersion.split(" ")[0].split("\\.")[index]);
            int behind = remote - local;

            return behind;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}