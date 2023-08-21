package me.deecaad.core.placeholder;

import me.deecaad.core.MechanicsCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;

import static me.deecaad.core.placeholder.PlaceholderAPI.PLACEHOLDER_PATTERN;

/**
 * Represents a message template that can contain placeholders.
 * This class is used to process and replace these placeholders with desired values.
 */
public class PlaceholderMessage {

    private final String template;
    private final Set<String> presentPlaceholders;

    /**
     * Constructs a new PlaceholderMessage based on the provided template string.
     *
     * @param template The template string containing potential placeholders.
     * @throws InvalidPlaceholderException If a placeholder in the template does not have a corresponding handler.
     */
    public PlaceholderMessage(String template) throws InvalidPlaceholderException {
        this(template, Collections.emptySet());
    }

    /**
     * Constructs a new PlaceholderMessage based on the provided template string.
     *
     * <p>Custom placeholders (placeholders not registered with a {@link PlaceholderHandler})
     * may be used if you add them.
     *
     * @param template                  The template string containing potential placeholders.
     * @param allowedCustomPlaceholders Any allowed custom placeholders (with the % before and after)
     * @throws InvalidPlaceholderException If a placeholder in the template does not exist.
     */
    public PlaceholderMessage(String template, Set<String> allowedCustomPlaceholders) throws InvalidPlaceholderException {
        Set<String> presentPlaceholders = new LinkedHashSet<>();

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder updatedTemplate = new StringBuilder();

        while (matcher.find()) {
            String placeholderString = matcher.group(1).toLowerCase();

            // Make sure the placeholder is valid
            PlaceholderHandler placeholderHandler = PlaceholderAPI.getPlaceholder(placeholderString);
            if (placeholderHandler == null && !allowedCustomPlaceholders.contains(PlaceholderAPI.addPercentSigns(placeholderString))) {
                throw new InvalidPlaceholderException(placeholderString, allowedCustomPlaceholders);
            }

            presentPlaceholders.add(placeholderString);

            // Replace the found placeholder in the template with its lowercase version
            matcher.appendReplacement(updatedTemplate, "%" + placeholderString + "%");
        }

        matcher.appendTail(updatedTemplate);

        this.template = updatedTemplate.toString();  // Update the template with the lowercase placeholders
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
     * and the values are all set to null.
     *
     * @param player    The player involved, or null.
     * @param itemStack The item involved, or null.
     * @param itemTitle The item title in config, or null.
     * @param slot      The equipment slot the item is held in, or null.
     * @return A map with placeholder handlers as keys and null as values.
     */
    public Map<String, Object> generateMap(
            @Nullable Player player,
            @Nullable ItemStack itemStack,
            @Nullable String itemTitle,
            @Nullable EquipmentSlot slot
    ) {
        Map<String, Object> resultMap = new HashMap<>(presentPlaceholders.size());
        for (String placeholder : presentPlaceholders) {
            PlaceholderHandler handler = PlaceholderAPI.getPlaceholder(placeholder);
            if (handler != null)
                resultMap.put(placeholder, handler.onRequest(player, itemStack, itemTitle, slot));
        }

        return resultMap;
    }

    /**
     * Replaces the placeholders in the template with the corresponding values provided in the replacements map.
     *
     * @param replacements A map containing PlaceholderHandlers as keys and the desired replacement values as values.
     * @return A new string where placeholders in the template have been replaced with the corresponding values.
     */
    public String replacePlaceholders(Map<String, Object> replacements) {
        String result = template;
        for (String placeholder : presentPlaceholders) {
            result = result.replace(placeholder, String.valueOf(replacements.get(placeholder)));
        }
        return result;
    }

    public Component replaceAndDeserialize(
            @Nullable Player player,
            @Nullable ItemStack itemStack,
            @Nullable String itemTitle,
            @Nullable EquipmentSlot slot,
            @Nullable Map<String, Object> customTags
    ) {
        Map<String, Object> tags = generateMap(player, itemStack, itemTitle, slot);
        if (customTags != null) tags.putAll(customTags);

        PlaceholderRequestEvent event = new PlaceholderRequestEvent(player, itemStack, itemTitle, slot, tags);
        Bukkit.getPluginManager().callEvent(event);

        String message = replacePlaceholders(event.getRequests());
        return MechanicsCore.getPlugin().message.deserialize(message);
    }
}
