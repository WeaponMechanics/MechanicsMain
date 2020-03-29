package me.deecaad.core.placeholder;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderAPI {

    private static Map<String, PlaceholderHandler> placeholderHandlers = new HashMap<>();
    private static final Pattern PLACEHOLDERS = Pattern.compile("[%]([^%,^\\s]+)[%]");

    /**
     * Same placeholder name can't be added twice
     *
     * @param placeholderHandler the new placeholder handler
     */
    public static void addPlaceholderHandler(PlaceholderHandler placeholderHandler) {
        if (placeholderHandlers.get(placeholderHandler.getPlaceholderName()) != null) {
            DebugUtil.log(LogLevel.ERROR,
                    "Tried to add placeholder handler with same name twice (" + placeholderHandler.getPlaceholderName() + ").",
                    "Ignoring this new placeholder...");
            return;
        }
        Matcher matcher = PLACEHOLDERS.matcher(placeholderHandler.getPlaceholderName());
        if (!matcher.find()) {
            DebugUtil.log(LogLevel.ERROR,
                    "Tried to add placeholder handler which format wasn't valid (" + placeholderHandler.getPlaceholderName() + ").",
                    "Correct format:",
                    "- has placeholder between % chars. (example %my-placeholder%)",
                    "- does not contain any whitespaces");
            return;
        }
        placeholderHandlers.put(placeholderHandler.getPlaceholderName(), placeholderHandler);
    }

    /**
     * Applies all possible placeholders. Includes Clip's PlaceholderAPI support.
     *
     * @param to the string where to apply placeholders
     * @param tempPlaceholders temporary placeholders
     * @param player the player involved in event or null
     * @param itemStack the itemstack involved in event or null
     * @param weaponTitle the weapon title involved in this request, can be null
     * @return the string with applied placeholders
     */
    public static String applyPlaceholders(String to, @Nullable Map<String, PlaceholderHandler> tempPlaceholders, @Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle) {
        if (to == null) {
            return null;
        }
        if (!placeholderHandlers.isEmpty()) {
            Matcher matcher = PLACEHOLDERS.matcher(to);
            while (matcher.find()) {
                String currentPlaceholder = matcher.group(1);
                PlaceholderHandler placeholderHandler = placeholderHandlers.get("%" + currentPlaceholder.toLowerCase() + "%");
                if (placeholderHandler == null) {

                    if (tempPlaceholders != null) {
                        placeholderHandler = tempPlaceholders.get("%" + currentPlaceholder.toLowerCase() + "%");
                        if (placeholderHandler == null) {
                            continue;
                        }
                    }

                    continue;
                }
                String request = null;
                try {
                    request = placeholderHandler.onRequest(player, itemStack, weaponTitle);
                } catch (Exception e) {
                    DebugUtil.log(LogLevel.WARN, "Placeholder using keyword %" + placeholderHandler.getPlaceholderName() + "% caused this exception!", e);
                }
                if (request == null) {
                    continue;
                }
                to = to.replace("%" + currentPlaceholder + "%", request);
            }
        }
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            to = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, to);
        } catch (ClassNotFoundException e) {/**/}
        return ChatColor.translateAlternateColorCodes('&', to);
    }

    /**
     * This should be called when reloading or shutting down server!
     */
    public static void onDisable() {
        placeholderHandlers.clear();
    }
}