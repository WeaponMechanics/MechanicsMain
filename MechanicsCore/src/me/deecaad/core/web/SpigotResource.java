package me.deecaad.core.web;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SpigotResource {

    private String resourceName;
    private String resourceCurrentVersion;
    private String resourceID;
    private String resourceLatestVersionURL;
    private String resourceUpdatesURL;
    private String latestUpdateVersion;
    private String latestUpdateTitle;
    private List<String> latestUpdateDescription;

    public SpigotResource(Plugin plugin, String resourceID) {
        this.resourceName = plugin.getDescription().getName();
        this.resourceCurrentVersion = plugin.getDescription().getVersion();
        this.resourceID = resourceID;
        this.resourceLatestVersionURL = "http://api.spiget.org/v2/resources/" + resourceID + "/versions/latest";
        this.resourceUpdatesURL = "http://api.spiget.org/v2/resources/" + resourceID + "/updates";
    }

    /**
     * Updates version, title and description based on resource ID.
     * Remember to run this asynchronously.
     * This also assumes that you want to fetch new version, title and description.
     *
     */
    public void update() {
        update(true, true);
    }

    /**
     * Updates version, title and description based on resource ID.
     * Remember to run this asynchronously.
     *
     * @param fetchTitle true if title should be fetched
     * @param fetchDescription true if description should be fetched
     */
    public void update(boolean fetchTitle, boolean fetchDescription) {
        updateVersion();
        updateDescription(fetchTitle, fetchDescription);
    }

    /**
     * Updates version based on resource ID.
     * Remember to run this asynchronously.
     */
    public void updateVersion() {
        try {
            URL url = new URL(this.resourceLatestVersionURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", this.resourceName);
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(inputStreamReader);
            this.latestUpdateVersion = (String) jsonObject.get("name");
        } catch (IOException e) {
            this.latestUpdateVersion = "Unknown";
            if (e.toString().contains("Server returned HTTP response code: 400")) {
                return;
            }
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
            this.latestUpdateVersion = "Unknown";
        }
    }

    /**
     * Updates title and description based on resource ID.
     * Remember to run this asynchronously.
     *
     * @param fetchTitle true if title should be fetched
     * @param fetchDescription true if description should be fetched
     */
    public void updateDescription(boolean fetchTitle, boolean fetchDescription) {
        if (this.latestUpdateVersion.equals("Unknown") || (!fetchTitle && !fetchDescription)) {
            return;
        }
        try {
            URL url = new URL(this.resourceUpdatesURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", this.resourceName);
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            JSONArray jsonArray = (JSONArray) new JSONParser().parse(inputStreamReader);
            JSONObject jsonObject = (JSONObject) jsonArray.get(jsonArray.size() - 1);
            if (fetchTitle) {
                this.latestUpdateTitle = jsonObject.get("title").toString();
            }
            if (fetchDescription) {
                byte[] decodedDescriptionByte = Base64.getDecoder().decode(jsonObject.get("description").toString());
                String decodedDescription = new String(decodedDescriptionByte, StandardCharsets.UTF_8);
                String[] splittedDecodedDescription = decodedDescription.split("\n");
                List<String> descriptionList = new ArrayList<>();
                for (String decodeHtml : splittedDecodedDescription) {
                    String string = decodeHtml.replaceAll("(<.*?>)\\1*", " ");
                    if (!string.contains("Code (Text):") && !string.trim().isEmpty()) {
                        descriptionList.add(string);
                    }
                }
                if (descriptionList.isEmpty()) {
                    return;
                }
                this.latestUpdateDescription = descriptionList;
            }

        } catch (IOException e) {
            this.latestUpdateTitle = "Unknown";
            if (e.toString().contains("Server returned HTTP response code: 400")) {
                return;
            }
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
            this.latestUpdateTitle = "Unknown";
        }
    }

    /**
     * @return the resource name
     */
    public String getResourceName() {
        return this.resourceName;
    }

    /**
     * @return the current (local) version of resource
     */
    public String getResourceCurrentVersion() {
        return this.resourceCurrentVersion;
    }

    /**
     * @return the given resource id
     */
    public String getID() {
        return this.resourceID;
    }

    /**
     * Version has to be manually updated using update methods given in this class.
     *
     * @return the version of latest update
     */
    public String getLatestUpdateVersion() {
        if (this.latestUpdateVersion == null) {
            return "Unknown";
        }
        return this.latestUpdateVersion;
    }

    /**
     * Title has to be manually updated using update methods given in this class.
     *
     * @return the title of latest update
     */
    public String getLatestUpdateTitle() {
        if (this.latestUpdateTitle == null) {
            return "Unknown";
        }
        return this.latestUpdateTitle;
    }

    /**
     * Description has to be manually updated using update methods given in this class.
     *
     * @return the description of latest update
     */
    public List<String> getLatestUpdateDescription() {
        return this.latestUpdateDescription;
    }
}