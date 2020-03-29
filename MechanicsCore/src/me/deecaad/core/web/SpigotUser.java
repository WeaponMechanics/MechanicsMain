package me.deecaad.core.web;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SpigotUser {

    private String userID;
    private String userURL;
    private String username;

    public SpigotUser(String userID) {
        this.userID = userID;
        this.userURL = "https://api.spiget.org/v2/authors/" + userID;
    }

    /**
     * Updates username based on user ID.
     * Remember to run this asynchronously.
     *
     * @param plugin the plugin instance
     */
    public void update(Plugin plugin) {
        try {
            URL url = new URL(SpigotUser.this.userURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", plugin.getDescription().getName());
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(inputStreamReader);
            SpigotUser.this.username = (String) jsonObject.get("name");
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
     * @return the given user id
     */
    public String getID() {
        return this.userID;
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

    /**
     * Username has to be manually updated using update(Plugin plugin) method in this class.
     *
     * @return the full user name based on id including used id
     */
    public String getFullUsername() {
        return getUsername() + " (" + this.userID + ")";
    }
}