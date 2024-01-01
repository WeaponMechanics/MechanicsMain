package me.deecaad.core.placeholder;

import me.deecaad.core.mechanics.Registry;
import me.deecaad.core.utils.Keyable;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;

/**
 * A PlaceholderHandler is a variable in a string. Instances of this class
 * return the current value of the variable, which can then be used in the string.
 */
public abstract class PlaceholderHandler implements Keyable {

    public static final Registry<PlaceholderHandler> REGISTRY = new Registry<>("Placeholders");

    private final String placeholderName;

    public PlaceholderHandler(@TagPattern String placeholderName) {
        this.placeholderName = placeholderName;
    }

    @Override
    public @NotNull String getKey() {
        return getPlaceholderName();
    }

    /**
     * Returns the lowercase id for this placeholder without any formatting.
     * The returned value will not have the diamond characters.
     *
     * @return This placeholder's name
     */
    public @NotNull String getPlaceholderName() {
        return this.placeholderName;
    }

    /**
     * Returns the value for this placeholder, given the <code>data</code>.
     *
     * @param data The data used to generate placeholders from
     * @return the result for placeholder or null
     */
    @Nullable
    public abstract String onRequest(@NotNull PlaceholderData data);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceholderHandler that = (PlaceholderHandler) o;
        return Objects.equals(placeholderName, that.placeholderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeholderName);
    }
}
