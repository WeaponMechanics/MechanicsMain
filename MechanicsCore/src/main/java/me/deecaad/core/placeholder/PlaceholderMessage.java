package me.deecaad.core.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.deecaad.core.MechanicsCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a message template that can contain placeholders.
 * This class is used to process and replace these placeholders with desired values.
 */
public class PlaceholderMessage {

    public static final @RegExp String TAG = "<([a-zA-Z_\\-]+)>";
    public static final Pattern TAG_PATTERN = Pattern.compile(TAG);

    private final String template;
    private final Set<String> presentPlaceholders;

    /**
     * Constructs a new PlaceholderMessage based on the provided template string.
     *
     * <p>Custom placeholders (placeholders not registered with a {@link PlaceholderHandler})
     * may be used if you add them.
     *
     * @param template                  The template string containing potential placeholders.
     */
    public PlaceholderMessage(@NotNull String template) {
        Set<String> presentPlaceholders = new LinkedHashSet<>();

        Matcher matcher = TAG_PATTERN.matcher(template);

        while (matcher.find()) {
            String placeholderString = matcher.group(1).toLowerCase();

            // Make sure the placeholder is valid
            PlaceholderHandler placeholderHandler = PlaceholderHandler.REGISTRY.get(placeholderString);
            if (placeholderHandler != null) {
                presentPlaceholders.add(placeholderString);
            }
        }

        this.template = template;
        this.presentPlaceholders = Collections.unmodifiableSet(presentPlaceholders);
    }

    /**
     * Returns the template string with all placeholders converted to lowercase.
     *
     * @return The processed template string.
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Gets an immutable set of PlaceholderHandler objects that were identified in the template.
     *
     * @return A set of identified placeholders.
     */
    public Set<String> getPresentPlaceholders() {
        return presentPlaceholders;
    }

    /**
     * Generates a map where the keys are the identified placeholders in the template,
     * and the values are all set to null. The placeholders are added to the map stored
     * in <code>data</code>.
     *
     * @param data The data to pass to the placeholder handlers.
     */
    public void fillMap(PlaceholderData data) {
        for (String placeholder : presentPlaceholders) {
            PlaceholderHandler handler = PlaceholderHandler.REGISTRY.get(placeholder);
            data.placeholders().put(placeholder, handler == null ? null : handler.onRequest(data));
        }
    }

    /**
     * Replaces the placeholders in the template with the corresponding values provided in the replacements map.
     *
     * @param replacements A map containing PlaceholderHandlers as keys and the desired replacement values as values.
     * @return A new string where placeholders in the template have been replaced with the corresponding values.
     */
    public String replacePlaceholders(Map<String, String> replacements) {
        String result = template;
        for (String placeholder : presentPlaceholders) {
            result = result.replace(placeholder, String.valueOf(replacements.get(placeholder)));
        }
        return result;
    }

    public Component replaceAndDeserialize(PlaceholderData data) {
        fillMap(data);

        // Let other plugins customize the appearance of placeholders
        PlaceholderRequestEvent event = new PlaceholderRequestEvent(data);
        Bukkit.getPluginManager().callEvent(event);

        // Let PlaceholderAPI
        String message = template;
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderAPI.setPlaceholders(data.player(), message);
        }

        // Convert the placeholder map into the tag resolver format for the adventure api
        Map<String, String> placeholders = event.placeholders();
        TagResolver[] tagResolvers = new TagResolver[event.placeholders().size()];
        int i = 0;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            tagResolvers[i++] = Placeholder.parsed(entry.getKey(), entry.getValue());
        }

        // Adventure api does the heavy lifting
        return MechanicsCore.getPlugin().message.deserialize(message, tagResolvers);
    }
}
