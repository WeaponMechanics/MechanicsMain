package me.deecaad.core.placeholder;

import net.kyori.adventure.text.minimessage.tag.TagPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ListPlaceholderHandler extends PlaceholderHandler {

    public ListPlaceholderHandler(@TagPattern String placeholderName) {
        super(placeholderName);
    }

    @Nullable
    @Override
    public String onRequest(@NotNull PlaceholderData data) {
        List<String> list = requestValue(data);
        return list == null ? null : String.join(", ", list);
    }

    @Nullable
    public abstract List<String> requestValue(@NotNull PlaceholderData data);
}
