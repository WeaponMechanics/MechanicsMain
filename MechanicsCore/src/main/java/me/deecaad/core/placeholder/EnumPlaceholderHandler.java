package me.deecaad.core.placeholder;

import net.kyori.adventure.text.minimessage.tag.TagPattern;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class EnumPlaceholderHandler extends PlaceholderHandler {

    public EnumPlaceholderHandler(@TagPattern String placeholderName) {
        super(placeholderName);
    }

    public abstract @NotNull List<String> getOptions();
}
