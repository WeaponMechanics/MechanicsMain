package me.deecaad.core.web;

import me.deecaad.core.utils.TaskUtil;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SpigotUser {

    private final Plugin plugin;
    private URL userURL;

    private String username;

    public SpigotUser(Plugin plugin, String userID) {
        this.plugin = plugin;
        try {
            this.userURL = new URL("https://api.spiget.org/v2/authors/" + userID);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        TaskUtil.runAsync(plugin, () -> {
            update();
            return null;
        });
    }

    /**
     * Updates username based on user ID.
     */
    private void update() {
        try {
            HttpURLConnection connection = (HttpURLConnection) this.userURL.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", plugin.getDescription().getName());
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(inputStreamReader);
            this.username = (String) jsonObject.get("name");
        } catch (IOException e) {
            if (e.toString().contains("Server returned HTTP response code: 400")) {
                return;
            }
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Username has to be manually updated using update(Plugin plugin) method in this class.
     *
     * @return the user name based on id
     */
    public String getUsername() {
        if (this.username == null) {
            return "Unknown";
        }
        return this.username;
    }
}