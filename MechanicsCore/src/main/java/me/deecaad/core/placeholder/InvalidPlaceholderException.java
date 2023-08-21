package me.deecaad.core.placeholder;

import me.deecaad.core.file.SerializerOptionsException;

import java.util.HashSet;
import java.util.Set;

public class InvalidPlaceholderException extends Exception {

    private final String placeholder;
    private final Set<String> customPlaceholders;

    public InvalidPlaceholderException(String placeholder, Set<String> customPlaceholders) {
        super("Invalid placeholder '" + placeholder + "'");
        this.placeholder = placeholder;
        this.customPlaceholders = customPlaceholders;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public Set<String> getCustomPlaceholders() {
        return customPlaceholders;
    }

    /**
     * Helper function to convert this exception into a user readable serializer
     * exception.
     *
     * @param location The location of the error in file.
     * @return The converted exception.
     */
    public SerializerOptionsException getAsSerializerException(String location) {
        Set<String> options = new HashSet<>(customPlaceholders);
        options.addAll(PlaceholderAPI.listPlaceholders().stream().map(PlaceholderHandler::getPlaceholderName).toList());
        return new SerializerOptionsException(
                "Message",
                "Placeholder",
                options,
                placeholder,
                location
        );
    }
}
